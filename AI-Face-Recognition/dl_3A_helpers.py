import shutil
import os
import glob
import math
import cv2
import pandas as pd
import numpy as np
from keras.utils import np_utils
from copy import copy, deepcopy
from haar_helper_functions import denoise_img

# Define a function to create necessary directories and subdirectores
def create_data_dirs(image_3D_dir, dl_data_subdir, data_subdirs, person_names, home_dir, project_folder):
    data_dir = os.path.join(image_3D_dir, dl_data_subdir)
    train_data_dir, validation_data_dir, test_data_dir = data_subdirs
    os.chdir(os.path.expanduser(os.getcwd())) # Move back to home, i.e., home/ubuntu on this AWS VM image
    os.chdir(image_3D_dir) # Move to the directory in which the picture subdirectories/folders reside
    os.mkdir(dl_data_subdir)
    os.chdir(dl_data_subdir)
    os.mkdir(train_data_dir)
    os.mkdir(validation_data_dir)
    os.mkdir(test_data_dir)
    for model_state in data_subdirs:
        os.chdir(model_state)
        for name in person_names:
            os.mkdir(name)
        os.chdir('..') # Move back up a level for the next name
    os.chdir(home_dir) # Move back to home/ubuntu
    os.chdir(project_folder) # Move back to mlnd-capstone

# Define a function to put the data subsets (train, val, test) by person_name in such folder/directory hierarchy
def create_data_in_sets(image_3D_dir, data_dir, data_subdirs, person_names, class_sample_cnt, home_dir, project_folder):
    train_data_dir, validation_data_dir, test_data_dir = data_subdirs
    train_data_path = os.path.expanduser(os.path.join(data_dir,train_data_dir))
    validation_data_path = os.path.expanduser(os.path.join(data_dir, validation_data_dir))
    test_data_path = os.path.expanduser(os.path.join(data_dir, test_data_dir))
    data_paths = [train_data_path, validation_data_path, test_data_path]
    os.chdir(os.path.expanduser(os.getcwd())) # Move back to home, i.e., home/ubuntu on this AWS VM image
    os.chdir(data_dir) # Move to the directory in which the picture subdirectories/folders reside
    null_sample_cnt = 0 # This 4th (added 1st) count/index to split into train, val, & test w/o need for recursion
    train_sample_cnt = int(math.floor(class_sample_cnt * 0.8))
    val_sample_cnt = int((class_sample_cnt - train_sample_cnt)/2)
    test_sample_cnt = int(class_sample_cnt - train_sample_cnt - val_sample_cnt)
    sample_cnts = [null_sample_cnt, train_sample_cnt, val_sample_cnt, test_sample_cnt]
    idx_starts = [sum(list(sample_cnts[0:i+1])) for i in range(0,(len(sample_cnts) - 1))]
    idx_ends = [sum(list(sample_cnts[1:m])) for m in range(2,len(sample_cnts)+1)]
    for name in person_names:
        input_class_path =  os.path.expanduser(os.path.join(image_3D_dir,name))
        files_in = os.listdir(input_class_path)[0:class_sample_cnt]
        for data_path_idx, data_path in enumerate(data_paths):
            person_data_path = os.path.expanduser(os.path.join(data_path,name))
            files_to_path = files_in[idx_starts[data_path_idx]:idx_ends[data_path_idx]]
            for file_name in files_to_path:
                if file_name.endswith('.jpg') or file_name.endswith('.jpeg'):
                    shutil.copy(os.path.expanduser(os.path.join(image_3D_dir,name,file_name)),os.path.expanduser(person_data_path))
    os.chdir(home_dir) # Move back to home, i.e., home/ubuntu on this AWS VM image
    os.chdir(project_folder) # Move back to mlnd-capstone

    return train_sample_cnt, val_sample_cnt, test_sample_cnt

# Define a function to fetch the images from the data subsets (train, val, test) as lists
def fetch_data_sets(data_dir, data_subdirs, names, sample_cnts, cls_cnt):
    dirs_imgs = [folder for folder, _, _ in os.walk(data_dir) if os.path.basename(folder) in names]
    train_dirs, val_dirs, test_dirs = \
        dirs_imgs[0:cls_cnt], dirs_imgs[cls_cnt:2*cls_cnt], dirs_imgs[2*cls_cnt:3*cls_cnt+1]
    imgs_train_RGB = [] # list of training images (np.arrays) in RBG and denoised
    labels_train = [] # list of corresponding labels (names) for training images
    imgs_val_RGB = [] # list of validation images (np.arrays)in RBG and denoised
    labels_val = [] # list of corresponding labels (names) for validation images
    imgs_test_RGB = [] # list of testing images (np.arrays)in RBG and denoised
    labels_test = [] # lists of corresponding labels (names) for testing images
    imgs_train_RGB = [denoise_img(cv2.cvtColor(cv2.imread(os.path.join(train_dir,img_fid)), cv2.COLOR_BGR2RGB)) \
                  for train_dir in train_dirs for img_fid in os.listdir(train_dir)]
    print("The length of imgs_train_RGB is {}".format(len(imgs_train_RGB)))
    imgs_val_RGB = [denoise_img(cv2.cvtColor(cv2.imread(os.path.join(val_dir,img_fid)), cv2.COLOR_BGR2RGB)) \
                  for val_dir in val_dirs for img_fid in os.listdir(val_dir)]
    print("The length of imgs_val_RGB is {}".format(len(imgs_val_RGB)))
    imgs_test_RGB = [denoise_img(cv2.cvtColor(cv2.imread(os.path.join(test_dir,img_fid)), cv2.COLOR_BGR2RGB)) \
                  for test_dir in test_dirs for img_fid in os.listdir(test_dir)]
    print("The length of imgs_test_RGB is {}".format(len(imgs_test_RGB)))
    # Now, since the train, val, and test imgs were read in by folder/name append labels in that same order
    for label_list_idx, label_list in enumerate([labels_train, labels_val, labels_test]):
        for name in names:
            i = 0
            for i in range(0, sample_cnts[label_list_idx]):
                label_list.append(name)

    return imgs_train_RGB, labels_train, imgs_val_RGB, labels_val, imgs_test_RGB, labels_test

# Define a function to randomize the train, val, & test imgs, matched with their labels
def randomize_data_set(df_gr_truth):
    df_gr_truth_col_names = list(deepcopy(df_gr_truth.columns.values))
    col_names_ground_truth_changed = [name+'_gt' for name in df_gr_truth_col_names]
    df_randomized = deepcopy(df_gr_truth)
    df_randomized.columns = col_names_ground_truth_changed
    df_row_idxs = np.arange(len(df_gr_truth))
    np.random.shuffle(df_row_idxs)
    rand_img_idxs = np.array(df_row_idxs)
    df_randomized.insert(1, 'image_idx_rnd', rand_img_idxs)

    return df_randomized

# Define a function to randomize the images themselves and their labels
def randomize_set(imgs, df_gt_rnd, names, num_classes):
    idxs_rnd = df_gt_rnd["image_idx_rnd"].tolist()
    X_imgs = [imgs[i] for i in idxs_rnd]
    y_int = [int(df_gt_rnd["person_idx_gt"][j]) for j in idxs_rnd]
    y_names = [names[k] for k in y_int]
    y_vectors = np_utils.to_categorical(y_int, num_classes=num_classes).astype(int)

    return X_imgs, y_int, y_names, y_vectors
