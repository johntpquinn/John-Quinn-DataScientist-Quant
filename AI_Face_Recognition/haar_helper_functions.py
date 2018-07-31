import cv2
import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import math

# Define a function to store and return a ground truth dataframe
def store_ground_truth(person_names, name_image_counts):
    df_ground_truth = pd.DataFrame(columns=['image_idx','person_idx', 'person_name'])
    img_idx = 0
    for person_name_idx, person_name in enumerate(person_names):
        for i in range(name_image_counts[person_name_idx]):
            image_idx = img_idx
            pers_idx = person_name_idx
            pers_name = person_name
            img_entry = [image_idx, pers_idx, pers_name]
            df_ground_truth.loc[img_idx] = img_entry
            img_idx += 1
    return df_ground_truth

# Define a function to denoise the image
def denoise_img(img_RGB):
    noisy_image = np.copy(img_RGB)
    denoised_image = cv2.fastNlMeansDenoisingColored(noisy_image,None,3, 3, 7, 21)
    return denoised_image

# Define a function to return a face bounding box
def get_face_bound_box(img_RGB, face_cascade, zero_vector):
    # Convert the RGB cleaned image to grayscale
    gray_img = cv2.cvtColor(img_RGB, cv2.COLOR_RGB2GRAY)
    # Detect the faces in gray image
    faces_gray = face_cascade.detectMultiScale(gray_img, 1.1, 6)
    face_count = len(faces_gray)
    if face_count >= 1:
        x_face, y_face, w_face, h_face = keep_center_face(faces_gray, face_count)
    else:
        x_face, y_face, w_face, h_face = zero_vector
    return x_face, y_face, w_face, h_face

# Define a function to keep only the most prominent (assume most central in image) face
def keep_center_face(faces_found, face_count, img_center_x=125, img_center_y=125, buff_px_f=10):
    central_area = x_keep = y_keep = w_keep = h_keep = 0
    closest_to_center = np.sqrt(img_center_x**2 + img_center_y**2) # initialize at vector distance max
    for i in range(0, face_count):
        x_f, y_f, w_f, h_f = faces_found[i]
        center_face_x = math.floor(x_f + w_f/2)
        center_face_y = math.floor(y_f + h_f/2)
        distance_to_center = np.sqrt((img_center_x - center_face_x)**2 + (img_center_y - center_face_y)**2)
        if distance_to_center <= closest_to_center:
            closest_to_center = distance_to_center
            face_keep = x_keep, y_keep, w_keep, h_keep = x_f, y_f, w_f, h_f

    return (x_keep+buff_px_f), (y_keep-buff_px_f), (x_keep+w_keep-8*buff_px_f), (y_keep+h_keep-6*buff_px_f)

# Define a function to display the picture with the face surrounded by its bounding box
def draw_face_with_box(img_RGB_copy, box_coordinates):
    x_copy, y_copy, w_copy, h_copy = box_coordinates
    cv2.rectangle(img_RGB_copy, (x_copy,y_copy), (x_copy+w_copy,y_copy+h_copy), (255,0,0), 2)
    fig = plt.figure(figsize = (22,22))
    ax1 = fig.add_subplot(111)
    ax1.set_xticks(x_ticks)
    ax1.set_yticks(y_ticks)
    ax1.set_title('RGB Image with Bounding Box Added')
    ax1.imshow(img_RGB_copy)

# Define a function to return the left eye bounding box
def get_left_eye_bound_boxes(img_RGB, face_box_coordinates, eye_cascade_l, buff_px_e):
    x_f, y_f, w_f, h_f = face_box_coordinates
    # Convert the RGB cleaned image to grayscale
    gray_img = cv2.cvtColor(img_RGB, cv2.COLOR_RGB2GRAY)
    eyes_gray_l = eye_cascade_l.detectMultiScale(gray_img, 1.1, 5)
    eye_count = len(eyes_gray_l)
    if eye_count >= 1:
        eyes = eyes_gray_l.tolist()
        eye_l = keep_eye_nearest_center_face(eyes, eye_count, face_box_coordinates, buff_px_e)
    else:
        eye_l = zero_vector
    return eye_l

# Define a function to keep only the most prominent eye (assume closest to face center)
def keep_eye_nearest_center_face(eyes_found, eye_count, face_box_coordinates, buff_px_e):
    x_f, y_f, w_f, h_f = face_box_coordinates
    central_area = x_keep = y_keep = w_keep = h_keep = 0
    center_face = center_face_x, center_face_y = math.floor(x_f + w_f/2), math.floor(y_f + h_f/2)
    closest_to_center =  np.sqrt((x_f+w_f)**2 + (y_f+h_f)**2) # initialize at vector distance max
    for i in range(0, eye_count):
        x_e, y_e, w_e, h_e = eyes_found[i]
        if (x_e + w_e) > center_face_x: # left eye (Left & Right from perspective of person/subject, not of viewer)
            inner_eye_x = x_e
        else:
            inner_eye_x = x_e + w_e# right eye (Left & Right from perspective of person/subject, not of viewer)
        inner_eye_y = math.floor(y_e + h_e/2)
        distance_to_center = np.sqrt((center_face_x - inner_eye_x)**2 + (center_face_y - inner_eye_y)**2)
        if distance_to_center <= closest_to_center:
            closest_to_center = distance_to_center
            x_keep, y_keep, w_keep, h_keep = x_e, y_e, w_e, h_e

    return (x_keep-buff_px_e), (y_keep-buff_px_e), (w_keep+buff_px_e-8*buff_px_e), (h_keep+buff_px_e-6*buff_px_e)

# THE BELOW FUNCTIONS ARE PROVIDED IF THE READER IS INTERESTED BUT ARE NOT ACTUALLY IMPLEMETED IN THE JUPYTER NOTEBOOK

# Define a function to keep only the most prominent (assume most central in image) nose
buff_px_n = 0 # number of buffering pixels desired around nose bounding box 
def keep_center_nose(noses_found, nose_count, face_box_coordinates):
    x_f, y_f, w_f, h_f = face_box_coordinates 
    central_area = x_keep = y_keep = w_keep = h_keep = 0
    center_face = center_face_x, center_face_y = math.floor(x_f + w_f/2), math.floor(y_f + h_f/2) 
    closest_to_center = np.sqrt((x_f+w_f)**2 + (y_f+h_f)**2) # initialize at vector distance max
    for i in range(0, nose_count):
        x_n, y_n, w_n, h_n = noses_found[i]
        # Discard any noses found outside the face bounding box
        if x_n < x_f or (x_n+w_n) > (x_f+w_f) or y_n < y_f or (y_n+h_n) > (y_f+h_f):
            continue
        center_nose_x = math.floor(x_n + w_n/2)
        center_nose_y = math.floor(y_n + h_n/2)
        distance_to_center = np.sqrt((center_face_x - center_nose_x)**2 + (center_face_y - center_nose_y)**2)
        if distance_to_center <= closest_to_center:
            closest_to_center = distance_to_center
            x_keep, y_keep, w_keep, h_keep = x_n, y_n, w_n, h_n
                                                                
    return (x_keep-buff_px_n), (y_keep-buff_px_n), (w_keep+buff_px_n-8*buff_px_n), (h_keep+buff_px_n-6*buff_px_n)

# Define a function to keep only the most prominent (assume closest to center face)
buff_px_s = 0 # number of buffering pixels desired around eye bounding 
def keep_center_smile(smiles_found, smile_count, face_box_coordinates):
    x_f, y_f, w_f, h_f = face_box_coordinates
    center_face = center_face_x, center_face_y = math.floor(x_f + w_f/2), math.floor(y_f + h_f/2)
    central_area = x_keep = y_keep = w_keep = h_keep = 0
    closest_to_center = np.sqrt(img_center_x**2 + img_center_y**2) # initialize at vector distance max
    for i in range(0, smile_count):
        x_s, y_s, w_s, h_s = smiles_found[i]
        if x_s < x_f or (x_s+w_s) > (x_f+w_f) or y_s < y_f or (y_s+h_s) > (y_f+h_f):
            continue
        center_smile_x = math.floor(x_s + w_s/2)
        center_smile_y = math.floor(y_s + h_s/2)
        distance_to_center = np.sqrt((center_smile_x - center_face_x)**2 + (center_smile_y - center_face_y)**2)
        if distance_to_center <= closest_to_center:
            closest_to_center = distance_to_center
            smile_keep = x_keep, y_keep, w_keep, h_keep = x_s, y_s, w_s, h_s
                                                               
    return (x_keep+buff_px_s), (y_keep-buff_px_s), (x_keep+w_keep-8*buff_px_s), (y_keep+h_keep-6*buff_px_s)
