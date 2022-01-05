
import flask
import werkzeug


# Importing libraries
import numpy as np
import os
import cv2
import tensorflow as tf
import warnings
warnings.filterwarnings('ignore')
import tensorflow_addons as tfa
from tensorflow.keras.metrics import top_k_categorical_accuracy, categorical_accuracy
from tensorflow.keras.preprocessing import image


UPLOAD_FOLDER = 'D:\IIIT-D\Summer Semester\CAPSTONE PROJECT\Server Received Files'

ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg'}


app = flask.Flask(__name__)
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER

def top_2_acc(y_true, y_pred):
    return top_k_categorical_accuracy(y_true, y_pred, k=2)

def top_3_acc(y_true, y_pred):
    return top_k_categorical_accuracy(y_true, y_pred, k=3)


# Model loading
model = tf.keras.models.load_model('D:\\IIIT-D\\Summer Semester\\CAPSTONE PROJECT\\Saved Models\\Mobile_Net_Test_Acc_86_V2.h5',
                                    custom_objects={'top_2_acc':top_2_acc,'top_3_acc':top_3_acc})



                                    
def removeHair(file,image_id):
    
    src = cv2.imread(file)
    grayScale = cv2.cvtColor( src, cv2.COLOR_RGB2GRAY )
    kernel = cv2.getStructuringElement(1,(17,17))
    blackhat = cv2.morphologyEx(grayScale, cv2.MORPH_BLACKHAT, kernel)
    ret,thresh2 = cv2.threshold(blackhat,10,255,cv2.THRESH_BINARY)
    dst = cv2.inpaint(src,thresh2,1,cv2.INPAINT_TELEA)
    filename = image_id
    cv2.imwrite(os.path.join(UPLOAD_FOLDER , filename), dst)
    filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
    return filepath



    


def prepare_image(filepath):
    img = image.load_img(filepath, target_size=(224, 224))
    img_array = image.img_to_array(img)
    img_array_expanded_dims = np.expand_dims(img_array, axis=0)
    return tf.keras.applications.mobilenet.preprocess_input(img_array_expanded_dims)

@app.route('/', methods = ['GET', 'POST'])
def handle_request():
    imagefile = flask.request.files['image']
    filename = werkzeug.utils.secure_filename(imagefile.filename)
    print("\nReceived image File name : " + imagefile.filename)
    imagefile.save(filename)
    filepath = os.path.join(app.config['UPLOAD_FOLDER'], filename)
    hairRemovedImage = removeHair(filepath,filename)
    print(hairRemovedImage)
    preprocessed_image = prepare_image(filepath)
    predictions = model.predict(preprocessed_image)
    os.remove(hairRemovedImage)
    print(predictions)

    ans = ''

    if(np.argmax(predictions,axis=1) == 0):
        ans = 'Actinic Keratoses (akiec)'
    elif(np.argmax(predictions,axis=1) == 1):
        ans = 'Basal cell carcinoma (bcc)'
    elif(np.argmax(predictions,axis=1) == 2):
        ans = 'Benign keratosis (bkl)'
    elif(np.argmax(predictions,axis=1) == 3):
        ans = 'Dermatofibroma (df)'
    elif(np.argmax(predictions,axis=1) == 4):
        ans = 'Melanoma (mel)'
    elif(np.argmax(predictions,axis=1) == 5):
        ans ='Melanocytic nevi (nv)' 
    elif(np.argmax(predictions,axis=1) == 6):
        ans ='Vascular skin (vasc)'

    print(ans)
    return ans






app.run(host="0.0.0.0", port=5000, debug=False)