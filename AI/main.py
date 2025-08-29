import tensorflow as tf
import pandas as pd
import numpy as np
import re
from sklearn.model_selection import train_test_split

df = pd.read_csv("data.csv")

labels = df["label"].unique().tolist()
label_map = {label: i for i, label in enumerate(labels)}
df["label_id"] = df["label"].map(label_map)

def simple_tokenizer(text):
    text = text.lower()
    text = re.sub(r'[^a-z0-9@.\s]', '', text)  # keep @ and .
    return text.split()


all_tokens = [token for text in df["text"] for token in simple_tokenizer(text)]
vocab = sorted(list(set(all_tokens)))
word_index = {word: i+1 for i, word in enumerate(vocab)}  # 0 reserved for padding

max_len = 20
def text_to_seq(text):
    tokens = simple_tokenizer(text)
    seq = [word_index.get(token, 0) for token in tokens]
    seq = seq[:max_len]
    seq += [0] * (max_len - len(seq))  # padding
    return seq

sequences = np.array([text_to_seq(text) for text in df["text"]])
labels_array = df["label_id"].values

X_train, X_val, y_train, y_val = train_test_split(
    sequences, labels_array, test_size=0.2, random_state=42
)

model = tf.keras.Sequential([
    tf.keras.layers.Embedding(input_dim=len(vocab)+1, output_dim=64, input_length=max_len),
    tf.keras.layers.Bidirectional(tf.keras.layers.GRU(32)),
    tf.keras.layers.Dense(32, activation="relu"),
    tf.keras.layers.Dense(len(labels), activation="softmax")
])

model.compile(
    loss="sparse_categorical_crossentropy",
    optimizer="adam",
    metrics=["accuracy"]
)

history = model.fit(
    X_train, y_train,
    validation_data=(X_val, y_val),
    epochs=50,
    batch_size=8
)

converter = tf.lite.TFLiteConverter.from_keras_model(model)
converter.target_spec.supported_ops = [
    tf.lite.OpsSet.TFLITE_BUILTINS,   # default TFLite ops
    tf.lite.OpsSet.SELECT_TF_OPS      # enable TF ops fallback
]
converter._experimental_lower_tensor_list_ops = False

tflite_model = converter.convert()
open("intent_classifier.tflite", "wb").write(tflite_model)

# 8️⃣ Save vocabulary
with open("vocab.txt", "w") as f:
    for word in vocab:
        f.write(word + "\n")

print("Training complete. TFLite model and vocab saved.")
