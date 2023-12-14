from firebase_admin import storage
import pandas as pd

def upload_file_to_storage(filename):
    bucket = storage.bucket()
    blob = bucket.blob(filename.split('/')[-1])
    blob.upload_from_filename(filename)