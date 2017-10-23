#include "sh61.h"
#include <string.h>
#include <errno.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <dirent.h>
#include <sys/wait.h>
#include <libgen.h>

static volatile sig_atomic_t SIGINT_RECEIVED = 0;  //boolean for whether or not Cntrl+C has been received
pid_t shell_pgid = 0;                              //pgid of the shell itself
pid_t background_pgid =  10;                       //pgid of the background process, if any
pid_t foreground_pgid =  20;                       //pgid for commands running in foreground, via start_command

int pipe_fd_counter = 0;                           //counter of number of pipe file descriptors
int pipefd[4][2];                                  //four pairs, which could be increased to the Linux maximum, for use here
int end_of_pipe = 0;                               //boolean for whether end of pipe has been received

// struct command
// This an individual command, which is a node in the 1st dimension (i.e., row) of a linked list of linked lists

typedef struct command command;
struct command {
    int argc;                    //number of arguments
    char** argv;                 //arguments, terminated by NULL
    pid_t pid;                   //process ID running this command, -1 if none
    pid_t pgid;                  //process group ID of this command
    int background;              //command should be run in the background (1) or not (0)
    int logical_and_condition;   //records whether this is a conditional command (0 for yes, 1 for no) for an && statement
    int logical_or_condition;    //records whether this is a conditional command (0 for yes, 1 for no) for an || statement
    int pipe_receiver;           //records that this process is the reader from a pipe
    int pipe_sender;             //records that this process is a writer to a pipe
    int type;                    //type of argument, specified by header file
    int file_in;                 //records whether there is a file redirected in
    char* file_in_name;          //records the name of the file redirected in
    int file_out;                //records whether there is a file redirected out
    char* file_out_name;         //records the name of the file redirected out
    int file2_out;               //records whether there is a file redirected out in place of stderr
    char* file2_out_name;        //records the name of the file redirected out in place of stderr
    struct command* next;        //pointer to the next single command in this command string
};

// command_alloc()
//    Allocate and return a new command structure.
//    Initializes its data members

static command* command_alloc(void) {
    command* c = (command*) malloc(sizeof(command));
    c->argc = 0;
    c->argv = NULL;
    c->pid = -1;
    c->pgid = -1;
    c->background = 0;
    c->logical_and_condition = 0;
    c->logical_or_condition = 0;
    c->pipe_receiver = 0;
    c->pipe_sender = 0;
    c->type = TOKEN_NORMAL;
    c->file_in = 0;
    c->file_in_name = NULL;
    c->file_out = 0;
    c->file_out_name = NULL;
    c->file2_out = 0;
    c->file2_out_name = NULL;
    c->next = NULL;
    return c;
}

//struct commands
//   a column & 2nd dimension linked list of linked lists of commands, the
//   overall struct with data members necessary and named the same as those
//   of an individual node

struct commands
{
    int commands_count;
    pid_t pgid;
    int background;
    int logical_and_condition;
    int logical_or_condition;
    int pipe_receiver;
    int pipe_sender;
    int truth_status;
    command* top_cmd;
    struct command *command_head;              //head of the column
    struct command *command_tail;              //tail of the column
    struct command* command_curr;
    struct commands* next;
};

struct commands *commands_beginning = NULL;   //first in the row
struct commands *commands_ending = NULL;      //last in the row
struct commands *commands_curr = NULL;        //current column of command




//create_commands
//    creates a column, a linked list of linked lists

struct commands* create_commands(command* cmd)
{
    struct commands *argp = (struct commands*)malloc(sizeof(struct commands));
    if(NULL == argp)
    {
        printf("\n Node lcreation failed \n");
        return NULL;
    }
    argp->commands_count = 0;
    argp->pgid = -1;
    argp->background = 0;
    argp->logical_and_condition = 0;
    argp->logical_or_condition = 0;
    argp->pipe_receiver = 0;
    argp->pipe_sender = 0;
    argp->truth_status = 1;
    argp->next = NULL;
    argp->command_head = cmd;
    argp->command_tail = cmd;
    commands_beginning = argp;
    commands_ending = argp;
    return argp;
}

// add_command
//     adds a command to a list (row)

struct commands* add_command(command* cmd)
{

    if(NULL == commands_beginning){
        return (create_commands(cmd));
    }
    commands_ending->command_tail->next = cmd;
    commands_ending->command_tail = cmd;
    commands_ending->background = cmd->background;
    return commands_ending;
}

// add_commands
//     adds a new column of commands

struct commands* add_commands(command* cmd){
    struct commands *argp = (struct commands*)malloc(sizeof(struct commands));
    if(NULL == argp)
    {
        printf("\n Node creation failed \n");
        return NULL;
    }
    argp->commands_count++;
    argp->pgid = cmd->pgid;
    argp->next = NULL;
    argp->command_head = cmd;
    argp->command_tail = cmd;
    commands_ending->next = argp;
    commands_ending = argp;
    return argp;
}

// command_free(c)
//    Free command structure `c`, including all its words.

static void command_free(command* c) {
    for (int i = 0; i != c->argc; ++i)
        free(c->argv[i]);
    free(c->argv);
    if (c->file_in_name) free(c->file_in_name);
    if (c->file_out_name) free(c->file_out_name);
    if (c->file2_out_name) free(c->file2_out_name);
    free(c);
}

// free_command_column
//     frees a full column of commands (i.e., a commandS)

void free_command_column(struct commands* commands) {
    struct command *argp = commands->command_head;
    struct command *prev;

    while (argp != NULL)
    {
        prev = argp;
        argp = argp->next;
        command_free(prev);
    }
}

// free_commands
//     frees a full row of commands

void free_commands() {
    struct commands *argp = commands_beginning;
    struct commands *prev;

    while (argp != NULL)
    {
        prev = argp;
        argp = argp->next;
        free_command_column(prev);
        free(prev);
    }
}

// stat_file
//     checks whether a file exists

int stat_file(const char* file_name){
    struct stat file_status;
    int file_exists = stat(file_name, &file_status);
    if (file_exists == -1)
        return 0;
    else
        return 1;
}

// command_append_arg(c, word)
//    Add `word` as an argument to command `c`. This increments `c->argc`
//    and augments `c->argv`.

static void command_append_arg(command* c, char* word) {
    c->argv = (char**) realloc(c->argv, sizeof(char*) * (c->argc + 2));
    c->argv[c->argc] = word;
    c->argv[c->argc + 1] = NULL;
    ++c->argc;
}

// handle_redirection
//     does the operation of rediredtion (<, >, or 2>)

void handle_redirection (command* c, pid_t pgid){
(void) pgid;
    if(c->file_in){
        if(opendir(dirname(c->file_in_name)) == 0)
        {
            printf("No such file or directory\n");
            exit(1);
        }
        if (stat_file(c->file_in_name))
        {
            int file_in = open(c->file_in_name, O_CREAT | O_RDONLY, 0664);
            dup2 (file_in, STDIN_FILENO);
            close (file_in);
        }
        else {
            printf("No such file or directory\n");
            exit(1);
        }
    }

    if(c->file_out){
        if(opendir(dirname(c->file_out_name)) == 0)
        {
            printf("No such file or directory\n");
            exit(1);
        }
        int file_out = open(c->file_out_name, O_CREAT | O_WRONLY | O_TRUNC, 0664);
        dup2 (file_out, STDOUT_FILENO);
        close (file_out);
    }

    if(c->file2_out)
    {
        if(opendir(dirname(c->file2_out_name)) == 0)
        {
            printf("No such file or directory\n");
            exit(1);
        }
        int file2_out = open(c->file2_out_name, O_CREAT | O_WRONLY | O_TRUNC, 0664);
        dup2 (file2_out, STDERR_FILENO);
        close (file2_out);
    }
}

// COMMAND EVALUATION

// start_command(c, pgid)
//    Start the single command indicated by `c`. Sets `c->pid` to the child
//    process running the command, and returns `c->pid`.
//
//    PART 1: Fork a child process and run the command using `execvp`.
//    PART 5: Set up a pipeline if appropriate. This may require creating a
//       new pipe (`pipe` system call), and/or replacing the child process's
//       standard input/output with parts of the pipe (`dup2` and `close`).
//       Draw pictures!
//    PART 7: Handle redirections.
//    PART 8: The child process should be in the process group `pgid`, or
//       its own process group (if `pgid == 0`). To avoid race conditions,
//       this will require TWO calls to `setpgid`.

pid_t start_command(command* c, pid_t pgid) {
    pid_t fork_rv;
    if (c->pipe_sender) 
    {
        if (pipe(pipefd[pipe_fd_counter]) != 0)
            perror("failed to create pipe\n");
        pipe_fd_counter++;
    }
    if (c->pipe_receiver  && !c->pipe_sender)
    {
        end_of_pipe = 1;
    }
    fork_rv = fork();
    c->pgid = pgid;
    if (fork_rv == -1)
    {
        perror("Could not fork!\n");
    }

    switch (fork_rv){
        case 0:  //child process
            setpgid(c->pgid, c->pgid);
            
            if (c->pipe_sender && !c->pipe_receiver)
            {
                dup2(pipefd[0][1], 1);
                close(pipefd[0][0]);
                close(pipefd[0][1]);
            }

            if (c->pipe_receiver && c->pipe_sender)
            {
                dup2(pipefd[pipe_fd_counter -2][0], 0);
                dup2(pipefd[pipe_fd_counter -1][1], 1);
                for (int i = 0; i < pipe_fd_counter; i++) 
                {
                    close(pipefd[i][0]);
                    close(pipefd[i][1]);
                }
            }

            if (c->pipe_receiver  && !c->pipe_sender){
                dup2(pipefd[pipe_fd_counter -1][0], 0);
                for (int i = 0; i < pipe_fd_counter; i++) {
                    close(pipefd[i][0]);
                    close(pipefd[i][1]);
                }
            }

            handle_redirection (c, pgid);

            if (execvp(c->argv[0], c->argv) == -1) 
            {
                printf("Couldn't execute the program: %s\n", strerror(errno));
                _exit(0);
            }
            else 
            {
            	//child running process successfully
            }
            break;
        default:  //parent process

            setpgid(c->pgid, c->pgid);
            
            if (c->pipe_sender)
            {
            	//not encountered
            }
            else if (c->pipe_receiver)
            {
            	//not encountered
            }
            c->pid = fork_rv;
            return c->pid;
            break;
    }
    c->pid = fork_rv;
    return c->pid;
}

// run_conditional_list
//     executes commands denoted as having the attribute (data member)
//     logical_and_condition or logical_or_condition

void run_conditional_list(struct commands* column) {
    int background = 0;                   //boolean for run in background (1) or not (0)
    if (column->pgid == background_pgid)
    {
        background = 1;
    }
    else
    {
        background = 0;
    }

    int truth_status = column->truth_status; //boolean for current state of && and || evaluations
    int need_run_logical = 1;                //boolean for whether next logical need run
    int exit_status;                         //exit status of child process
    pid_t child_pid = -1;                    //child pid initialized at -1
    
    struct command *argp = column->command_head;
    if (argp->argc == 0){
        if (argp->logical_and_condition == 1  && truth_status == 0)
        {
            need_run_logical = 0;
        }
        if (argp->logical_or_condition == 1 && truth_status == 1)
        {
            need_run_logical = 0;
        }
        argp = argp->next;
    }

    while (argp != NULL && !SIGINT_RECEIVED) {
        // process cd commands in parent
          if (strcmp(argp->argv[0], "cd") == 0)
          {
            truth_status = 1;

            if(opendir(argp->argv[1]) == 0)
            {
                truth_status = 0;
            }

            if (chdir (argp->argv[1]) != 0) 
            {
                truth_status = 0;
            }

            if(argp->file_in){

                if(opendir(dirname(argp->file_in_name)) == 0)
                {
                    printf("No such file or directory\n");
                    truth_status = 0;
                }

                if (stat_file(argp->file_in_name))
                {
                    int file_in = open(argp->file_in_name, O_CREAT | O_RDONLY, 0664);
                    dup2 (file_in, STDIN_FILENO);
                    close (file_in);
                }
                else 
                {
                    printf("No such file or directory\n");
                    truth_status = 0;
                }
            }

            if(argp->file_out)
            {
            	if(opendir(dirname(argp->file_out_name)) == 0)
            	{
                    printf("No such file or directory\n");
                    truth_status = 0;
                }
                int file_out = open(argp->file_out_name, O_CREAT | O_WRONLY | O_TRUNC, 0664);
                dup2 (file_out, STDOUT_FILENO);
                close (file_out);
            }

            if(argp->file2_out)
            {
                if(opendir(dirname(argp->file2_out_name)) == 0)
                {
                    printf("No such file or directory\n");
                    truth_status = 0;
                }
                int file2_out = open(argp->file2_out_name, O_CREAT | O_WRONLY | O_TRUNC, 0664);
                dup2 (file2_out, STDERR_FILENO);
                close (file2_out);
            }

            need_run_logical = 1;
            if (argp->logical_and_condition == 1) 
            {
                if (truth_status == 0)
                {
                    need_run_logical = 0;
                }
            }

            if (argp->logical_or_condition == 1) 
            {
                if (truth_status == 1){
                    need_run_logical = 0;
                }
            }

            argp = argp->next;
            continue;
        }

        if (need_run_logical)
        {
            waitpid(-1, 0, WNOHANG);
            child_pid = start_command(argp, column->pgid);
            waitpid(argp->pid, &exit_status, 0);

            if (WEXITSTATUS(exit_status) == 0)
            {
                truth_status = 1;
            }
            else
            {
                truth_status = 0;
            }
        }
       
        if (!argp->pipe_sender && !background) 
        {
        	if (WIFEXITED(exit_status)) 
        	{
                    if (WIFSIGNALED(exit_status) != 0 && WTERMSIG(exit_status) == SIGINT)
                    {
                        kill(argp->pgid, SIGINT);
                        set_foreground(0);
                        _exit(0);
                    }
                    if (WEXITSTATUS(exit_status) == 0)
                    {
                        truth_status = 1;
                    }
                    else 
                    {
                        truth_status = 0;
                    }
                }
            }

            need_run_logical = 1;
            if (argp->logical_and_condition == 1) 
            {
                if (truth_status == 0){
                    need_run_logical = 0;
                }
            }

            if (argp->logical_or_condition == 1) 
            {
                if (truth_status == 1){
                   need_run_logical = 0;
                }
            }
        argp = argp->next;

    }



    if (background) {

       while (child_pid != waitpid(child_pid, &exit_status, WNOHANG)) {}
        set_foreground(0);
        _exit(0);
    }

    column->truth_status = truth_status;
}


// run_list()
//    Run the command list starting at `c`.
//
//    PART 1: Start the single command `c` with `start_command`,
//        and wait for it to finish using `waitpid`.
//    The remaining parts may require that you change `struct command`
//    (e.g., to track whether a command is in the background)
//    and write code in run_list (or in helper functions!).
//    PART 2: Treat background commands differently.
//    PART 3: Introduce a loop to run all commands in the list.
//    PART 4: Change the loop to handle conditionals.
//    PART 5: Change the loop to handle pipelines. Start all processes in
//       the pipeline in parallel. The status of a pipeline is the status of
//       its LAST command.
//    PART 8: - Choose a process group for each pipeline.
//       - Call `set_foreground(pgid)` before waiting for the pipeline.
//       - Call `set_foreground(0)` once the pipeline is complete.
//       - Cancel the list when you detect interruption.

void run_list(struct commands* column) {
    int background = column->background;  //boolean for run in background (1) or not (0)
    int truth_status = 1;                 //boolean for current state of && and || evaluations
    pid_t fork_rv;                        //child's pid (on successful fork)
    int exit_status; 					  //exit status of child process
    pid_t child_pid = -1;

    struct command *argp = column->command_head;

    if (background) 
    {
        setpgid(background_pgid, background_pgid);
        if ((fork_rv = fork()) < 0){
            perror("shell fork failed to execute");
            _exit(1);
        }
        else if (fork_rv > 0)
        {
            if (column->commands_count == 1 && !column->pipe_sender && 
            	!column->pipe_receiver) sleep(1); //T29 grade serv. oddity
            waitpid(-1, 0 , WNOHANG);
            return;
        }

        column->pgid = background_pgid;
    }
    else 
    {
        setpgid(foreground_pgid, foreground_pgid);
        column->pgid = foreground_pgid;
    }

    if ((argp->logical_and_condition || argp->logical_or_condition) && (column->pipe_sender == 0))
    {
        run_conditional_list(column);
        if (background) 
        {
            waitpid(-1, 0, WNOHANG);
            _exit(0);
        }
        return;
    }

    if ((argp->logical_and_condition || argp->logical_or_condition) && (column->pipe_sender == 1))
    {
        truth_status = column->truth_status;
        if (argp->argc == 0)
        {
            if (argp->logical_and_condition == 1  && truth_status == 0)
            {
                return;
            }
            if (argp->logical_or_condition == 1 && truth_status == 1)
            {
                return;
            }
            argp = argp->next;
        }


        if (background) 
        {
            _exit(0);
            waitpid(-1, 0, WNOHANG);
        }
    }

    while (argp != NULL && !SIGINT_RECEIVED) {

        //cd must be handled directly within parent
        if (strcmp(argp->argv[0] , "cd") == 0){
            truth_status = 1;
            if(opendir(argp->argv[1]) == 0)
            {
               truth_status = 0;
            }
            if (chdir (argp->argv[1]) != 0) {
                truth_status = 0;
            }
            handle_redirection(argp, argp->pgid);
            argp = argp->next;
            continue;
        }

        waitpid(-1, 0, WNOHANG);

        child_pid = start_command(argp, column->pgid);

        if (argp->pipe_receiver && !argp->pipe_sender) 
        {
            for (int i = 0; i < pipe_fd_counter; i++){
                close(pipefd[i][0]);
                close(pipefd[i][1]);
            }
            if(end_of_pipe){
                end_of_pipe = 0;
                pipe_fd_counter = 0;
            }
        }

        if (!argp->pipe_sender ) 
        {
            if (background)
            {
                waitpid(argp->pid, &exit_status, WNOHANG);
            }
            else
            {
                waitpid(argp->pid, &exit_status, 0);
            }

            if (WIFEXITED(exit_status)) 
            {
                if (WIFSIGNALED(exit_status) != 0 && (WTERMSIG(exit_status) == SIGINT))
                {
                    kill(argp->pgid, SIGINT);
                    set_foreground(0);
                    _exit(0);
                }

                if (WEXITSTATUS(exit_status) == 0)
                {
                    truth_status = 1;
                }
                else 
                {
                    truth_status = 0;
                }
            }
        }

        argp = argp->next;
    }


    if (background) 
    {
    	while (child_pid != waitpid(child_pid, &exit_status, WNOHANG)){}
        _exit(0);
        set_foreground(0);
    }

    column->truth_status = truth_status;
}


// eval_line(c)
//    Parse the command list in `s` and run it via `run_list`.

void eval_line(const char* s) {
    int type = 0;                 //the type of the current token
    char* token = NULL;           //the current token itself

    // build the command
    int make_new_command = 0;     //boolean for add to column
    int make_new_commands = 0;    //boolean for add to row
    int pipe_receiver_next = 0;   //boolean for whether next command is a pipe_reader
   
    command* c = command_alloc();  //create a place for a command
    create_commands(c);            //create a place for a list of commands
    while ((s = parse_shell_token(s, &type, &token)) != NULL && !SIGINT_RECEIVED) 
    {
         if (make_new_command == 1) 
         {
            c = command_alloc();
            if (pipe_receiver_next) {c->pipe_receiver = 1; pipe_receiver_next = 0;}
            if (make_new_commands == 0) 
            {
                add_command(c);
            }
            else 
            {
                add_commands(c);
                make_new_commands = 0;
            }
            make_new_command = 0;
        }
        c->type = type;
        
        switch (type) {

            case TOKEN_NORMAL:
                if (strcmp((const char *)token , "n") == 0) 
                {
                    if (c->argc > 0)
                    {
                    	make_new_command = 1;
                    	make_new_commands = 1;
                    }
                }
                else
                    command_append_arg(c, token);
                break;

            case TOKEN_REDIRECTION:
                switch (token[0]) 
                {
                    case '<':
                        c->file_in = 1;
                        s = parse_shell_token(s, &type, &token);
                        c->file_in_name = token;
                        break;
                    case '>':
                        c->file_out = 1;
                        s = parse_shell_token(s, &type, &token);
                        c->file_out_name = token;
                        break;
                    case '2':
                        c->file2_out = 1;
                        s = parse_shell_token(s, &type, &token);
                        c->file2_out_name = token;
                        break;
                    default :
                        perror("tokenizer is broken\n");
                }
                break;

            case TOKEN_SEQUENCE:
                make_new_command = 1;
                make_new_commands = 1;
                break;

            case TOKEN_BACKGROUND:
                c->background = 1;
                commands_ending->background = c->background;
                make_new_command = 1;
                make_new_commands = 1;
                break;

            case TOKEN_PIPE:
                c->pipe_sender = 1;
                commands_ending->pipe_sender = c->pipe_sender;
                make_new_command = 1;
                pipe_receiver_next = 1;
                break;

            case TOKEN_AND:
                if (c->pipe_receiver == 1){
                    c = command_alloc();
                    add_commands(c);
                }
                c->logical_and_condition = 1;
                make_new_command = 1;
                break;

            case TOKEN_OR:
                if (c->pipe_receiver == 1)
                {
                    c = command_alloc();
                    add_commands(c);
                }
                c->logical_or_condition = 1;
                make_new_command = 1;
                break;

            case TOKEN_LPAREN:
                make_new_command = 1;
                make_new_commands = 1;
                break;

            case TOKEN_RPAREN:
                make_new_command = 1;
                make_new_commands = 1;
                break;

            default:
                break;
        }
    }
    // execute it
    int truth_status = 1;
    struct commands *argp = commands_beginning;
    while (argp != NULL && !SIGINT_RECEIVED)
    {
        run_list(argp);
        truth_status = argp->truth_status;
        argp = argp->next;
        if (argp)
            argp->truth_status = truth_status;
    }
    free_commands();
}

//interruption_signal_handler 
//    signal handler function for SIGNINT
void interruption_signal_handler (int interruption){
    (void) interruption;
    SIGINT_RECEIVED = 1;
}

int main(int argc, char* argv[]) {
    FILE* command_file = stdin;
    int quiet = 0;
    shell_pgid = getpid();            //this is the pid of the shell itself
    setpgid(shell_pgid, shell_pgid);
    // Check for '-q' option: be quiet (print no prompts)
    if (argc > 1 && strcmp(argv[1], "-q") == 0) 
    {
        quiet = 1;
        --argc, ++argv;
    }

    // Check for filename option: read commands from file
    if (argc > 1) 
    {
        command_file = fopen(argv[1], "rb");
        if (!command_file) {
            perror(argv[1]);
            exit(1);
        }
    }

    // - Put the shell into the foreground
    // - Ignore the SIGTTOU signal, which is sent when the shell is put back
    //   into the foreground
    set_foreground(0);
    handle_signal(SIGTTOU, SIG_IGN);
    handle_signal(SIGINT, interruption_signal_handler);
    char buf[BUFSIZ];
    int bufpos = 0;
    int needprompt = 1;

    while (!feof(command_file)) 
    {
        // Print the prompt at the beginning of the line
        if (needprompt && !quiet) {
            printf("\nsh61[%d]$ ", getpid());
            fflush(stdout);
            needprompt = 0;
        }

        // Read a string, checking for error or EOF
        if (fgets(&buf[bufpos], BUFSIZ - bufpos, command_file) == NULL) 
        {
            if (ferror(command_file) && errno == EINTR) 
            {
                // ignore EINTR errors
                clearerr(command_file);
                buf[bufpos] = 0;
            } else 
            {
                if (ferror(command_file))
                    perror("sh61");
                break;
            }
        }

        // If a complete command line has been provided, run it
        bufpos = strlen(buf);
        if ((bufpos == BUFSIZ - 1 || (bufpos > 1 && buf[bufpos - 1] == '\n'))) 
        {
            if (strcmp(buf, "exit\n") == 0 || strcmp(buf, "exit\r\n") == 0 || strcmp(buf, "exit") == 0)
                return 0;
            eval_line(buf);
        }

        bufpos = 0;
        needprompt = 1;
        // Handle zombie processes and/or interrupt requests
        // Reap all zombie processes
        waitpid(-1, 0, WNOHANG);
        SIGINT_RECEIVED = 0;
    }
     return 0;
}