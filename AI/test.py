import tensorflow as tf
import numpy as np
import re

# Load the TFLite model
interpreter = tf.lite.Interpreter(model_path="intent_classifier.tflite")
interpreter.allocate_tensors()
input_details = interpreter.get_input_details()
output_details = interpreter.get_output_details()

vocab = {}
with open("vocab.txt", "r") as f:
    for i, word in enumerate(f.read().splitlines(), start=1):  # start=1 to match training
        vocab[word] = i
max_len = 20

def simple_tokenizer(text):
    text = text.lower()
    text = re.sub(r'[^\w\s]', '', text)
    return text.split()

def text_to_seq(text):
    tokens = simple_tokenizer(text)
    seq = [vocab.get(token, 0) for token in tokens]  # unknown -> 0
    seq = seq[:max_len]
    seq += [0] * (max_len - len(seq))
    return np.array([seq], dtype=np.int32)

def predict_intent(text):
    input_data = text_to_seq(text).astype(np.float32)
    interpreter.set_tensor(input_details[0]['index'], input_data)
    interpreter.invoke()
    output_data = interpreter.get_tensor(output_details[0]['index'])
    predicted_class = np.argmax(output_data)
    return predicted_class, output_data

labels = ["change_email", "change_threshold", "disable_alerts", "enable_alerts", "other"]  # same order as training
text = "can you change the number of failed attempts to 5"
predicted_class, output_probs = predict_intent(text)
print("Predicted label:", labels[predicted_class])
print("Probabilities:", output_probs)
