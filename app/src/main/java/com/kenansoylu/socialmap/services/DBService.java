package com.kenansoylu.socialmap.services;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.kenansoylu.socialmap.data.PinData;

public class DBService {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getAllPins(OnSuccessListener<QuerySnapshot> onSuccessListener) {
        db.collection("pins").get().addOnSuccessListener(onSuccessListener);
    }

    public void subscribeToPins(EventListener<QuerySnapshot> eventListener) {
        db.collection("pins").addSnapshotListener(eventListener);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addPin(PinData pin, OnSuccessListener<DocumentReference> successListener) {
        db.collection("pins").add(pin.serialize()).addOnSuccessListener(successListener);
    }

    public void getUser(String id, OnCompleteListener<DocumentSnapshot> onCompleteListener) {
        db.collection("users").document(id).get().addOnCompleteListener(onCompleteListener);
    }
}
