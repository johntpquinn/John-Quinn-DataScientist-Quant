from __future__ import print_function
import argparse
import base64
import requests
import json
import sys
from googleapiclient import discovery
from googleapiclient import errors
from collections import OrderedDict
import numpy as np
from copy import deepcopy

# Global Variable: idx_2_labels
idxs = np.arange(0,29)
labels = ['IRW_BM2078606', 'IRW_BM2078608', 'IRW_BM2078610', 'IRW_BM2078612','IRW_BM2078700',
          'IRW_BM2078706','KTI_3239', 'PTY_FA-113-8TAVSE','PTY_J708L', 'PTY_J708S', 'PTY_J712S',
          'PTY_J715S', 'RTC_86907', 'RTC_86917', 'RTC_86922', 'RTC_86927', 'SBD_J795G', 'SBD_J795S',
          'SNY_85-610', 'SNY_85-762', 'SNY_87-367', 'SNY_87-369', 'SNY_87-471', 'SNY_87-473',
          'SNY_90-948','SNY_90-949', 'SNY_90-950', 'URR_708S', 'URR_795G']
idx_2_labels = OrderedDict({idx : label for idx, label in zip(idxs, labels)})

# ADAPTED FROM: https://github.com/GoogleCloudPlatform/
# python-docs-samples/blob/master/ml_engine/online_prediction/predict.py
# define a function to create the (GCP ML Engine default) service object
def create_service(PROJECTID, model_name):
    project_ID = 'projects/{}'.format(PROJECTID)
    model_IDENT = '{}/models/{}'.format(project_ID, model_name)
    ml_service = discovery.build('ml', 'v1')

    return ml_service, model_IDENT

# FROM: https://github.com/GoogleCloudPlatform/cloudml-samples/blob/master/flowers/images_to_json.py
# define a function to create the request payload's body
def create_payload(input_image_fid):
    with open(input_image_fid, 'rb') as image_file:
        jpeg_data = image_file.read()
    encoded_img = base64.b64encode(jpeg_data)
    instance_in = dict(b64=encoded_img)
    instance_in = json.loads(json.dumps(instance_in))
    pay_load_body = [instance_in]

    return pay_load_body

# define a function to return the top 15 labels
def get_top_n(respuesta, n=15):
    top_n_idxs = []
    global idx_2_labels
    preds = json.loads(json.dumps(respuesta))
    preds = preds['predictions']
    preds = preds[0]
    preds = preds['predictions']
    probs_top_n = deepcopy(preds)
    probs_top_n = sorted(probs_top_n, reverse=True)[0:n]
    for prob_top_n in probs_top_n:
        prob_top_n_idx = preds.index(prob_top_n)
        top_n_idxs.append(prob_top_n_idx)
    top_n_labels = [idx_2_labels[i] for i in top_n_idxs]

    print (top_n_labels)

def main(image_file_name):
    """Send user input to the prediction service."""
    project = 'hidden-layers-llc'
    model = 'tf_serving_keras_wrenches_Step4'
    payload_body = create_payload(image_file_name)
    ml, model_ID = create_service(project, model)
    payload = {"instances" : payload_body}
    request = ml.projects().predict(name=model_ID, body=payload)
    try:
        response = request.execute()
    except errors.HttpError as err:
        print('The following HTTP error occurred')
        print(err._get_reason())
        print('http error code: {}'.format(err))
    except RuntimeError as er:
        print('The following Runtime error occurred:')
        print(str(er))
    else:
        top_n = get_top_n(response, 15)
        #print(top_n)
        return top_n

if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '--image_file_name',
        help="Name of the image file, e.g., xxx.jpg",
        type=str,
        required=True
    )
    args=parser.parse_args()
    main(args.image_file_name)