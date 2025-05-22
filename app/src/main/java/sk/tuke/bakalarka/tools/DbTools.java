package sk.tuke.bakalarka.tools;



import static sk.tuke.bakalarka.tools.ParseTools.parseClothingItem;
import static sk.tuke.bakalarka.tools.ParseTools.parseClothingItemToHashMap;
import static sk.tuke.bakalarka.tools.ParseTools.parseLocalDateToString;
import static sk.tuke.bakalarka.tools.ParseTools.parseOutfit;
import static sk.tuke.bakalarka.tools.ParseTools.parseOutfitToHashMap;
import static sk.tuke.bakalarka.tools.ParseTools.parseStringToLocalDate;
import static sk.tuke.bakalarka.tools.ParseTools.parseSwapItemToHashMap;
import static sk.tuke.bakalarka.tools.ParseTools.parseToImageReference;
import static sk.tuke.bakalarka.tools.StatisticsCalculator.calculatePercentiles;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import sk.tuke.bakalarka.entities.ClothingItem;
import sk.tuke.bakalarka.entities.Outfit;
import sk.tuke.bakalarka.entities.SwapItem;

public class DbTools {
    public static CollectionReference parseToCollectionReference(String userId, String collection) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        return db.collection("user_clothes").document(userId).collection(collection);
    }
    public static void addClothingItemToDatabase(String userId, ClothingItem clothingItem, Context context) {
        CollectionReference collectionReference = parseToCollectionReference(userId,"clothes");
        addClothingItemToDatabase(collectionReference, clothingItem, new AddClothingItemCallback() {
            @Override
            public void onClothingItemAdded() {
                Toast.makeText(context,"Clothing Item Added Successfully",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClothingItemAddFailed(Exception e) {
                Toast.makeText(context,"Uh oh, something went wrong",Toast.LENGTH_SHORT).show();
            }
        });
    }


    public interface AddClothingItemCallback {
        void onClothingItemAdded();
        void onClothingItemAddFailed(Exception e);
    }

    public static void addClothingItemToDatabase(CollectionReference collectionReference, ClothingItem clothingItem, AddClothingItemCallback callback) {
        HashMap<String, Object> clothingItemMap = parseClothingItemToHashMap(clothingItem);
        DocumentReference documentReference = collectionReference.document(String.valueOf(clothingItem.getId()));
        addClothingItemToDatabase(documentReference, clothingItemMap, callback);

    }
    public static void addClothingItemToDatabase(DocumentReference documentReference, HashMap<String,Object> item, AddClothingItemCallback callback) {
        documentReference.set(item).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    callback.onClothingItemAdded();
                }else{
                    callback.onClothingItemAddFailed(task.getException());
                }
            }
        });
    }

    public static void updateClothingItemInDatabase(String userId, ClothingItem clothingItem, Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userClothes = db.collection("user_clothes").document(userId);
        DocumentReference clothingItemRef = userClothes.collection("clothes").document(String.valueOf(clothingItem.getId()));

        HashMap<String, Object> clothingItemMap = parseClothingItemToHashMap(clothingItem);

        clothingItemRef.update(clothingItemMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Clothing item updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, "Failed to update clothing item", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public interface OnUserClothingItemsCallback {
        void onUserClothingItemsLoaded(List<ClothingItem> clothingItems);
        void onError(Exception e);
    }


    public static void getUserClothingItems(String userId, String orderBy, boolean inLaundry,OnUserClothingItemsCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<ClothingItem> clothingItems = new ArrayList<>();
        CollectionReference clothesCollectionRef = db.collection("user_clothes").document(userId).collection("clothes");

        clothesCollectionRef.whereEqualTo("inLaundry",inLaundry).orderBy(orderBy).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    clothingItems.add(parseClothingItem(document));
                }
                callback.onUserClothingItemsLoaded(clothingItems);
            } else {
                callback.onError(task.getException());
            }
        });
    }


    public static void getUserClothingItems(Query query, OnUserClothingItemsCallback callback) {
        if(query == null) {
            return;
        }
        List<ClothingItem> clothingItems = new ArrayList<>();

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    clothingItems.add(parseClothingItem(document));
                }
                callback.onUserClothingItemsLoaded(clothingItems);
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public static void getUserClothingItemsByIds(String userId, List<String> ids, OnUserClothingItemsCallback callback) {
        if(ids == null) {
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<ClothingItem> clothingItems = new ArrayList<>();
        for(String id : ids) {
            DocumentReference clothingItemDocumentRef = db.collection("user_clothes")
                    .document(userId)
                    .collection("clothes").document(id);
            clothingItemDocumentRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if(documentSnapshot.exists()) {
                            clothingItems.add(parseClothingItem(task.getResult()));
                        }
                    }
                    callback.onUserClothingItemsLoaded(clothingItems);
                }
            });
        }
    }

    public interface OnClothingImageCallback {
        void onImageUploaded(String imageUrl);
        void onError(Exception e);
    }

    public static void uploadImageBytesToFirebaseStorage(String path, byte[] bytes, Context context, OnClothingImageCallback callback) {

        if(bytes == null) {
            return;
        }
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(path);
        storageReference.putBytes(bytes)
                .addOnSuccessListener(taskSnapshot -> {
                    Toast.makeText(context,"Photo uploaded successfully",Toast.LENGTH_SHORT).show();
                    storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri downloadUrl) {
                            //show image in imageview
                            String imageUrl = downloadUrl.toString();
                            callback.onImageUploaded(imageUrl);
                        }
                    });
                })
                .addOnFailureListener(e -> Toast.makeText(context,e.getMessage(),Toast.LENGTH_SHORT).show());
    }

    public static void updateImageUrl(String userId, String collection,String itemId, String imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference clothingItemDocumentRef = db.collection("user_clothes")
                .document(userId).collection(collection).document(itemId);
        clothingItemDocumentRef.update("imageLink",imageUrl);
    }

    public static void updateSwapImageUrl(String swapId, String imageUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference clothingItemDocumentRef = db.collection("swap").document(swapId);
        clothingItemDocumentRef.update("imageLink",imageUrl);
    }


    public static void toggleInLaundry(String userId, String clothingItemId, boolean inLaundry) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference clothingItemDocumentRef = db.collection("user_clothes")
                .document(userId).collection("clothes").document(clothingItemId);
        clothingItemDocumentRef.update("inLaundry",inLaundry);
    }

    public static void washClothingItem(String userId, String clothingItemId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference clothingItemDocumentRef = db.collection("user_clothes")
                .document(userId).collection("clothes").document(clothingItemId);
        clothingItemDocumentRef.update("timesWashed", FieldValue.increment(1));
    }

    public static void removeItemFromDatabase(String userId, String collection, String itemId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userClothes = db.collection("user_clothes").document(userId);
        userClothes.collection(collection).document(itemId).delete();
    }

    public static void removeClothingImageFromStorage(String imageRef) {
        if(imageRef == null || imageRef.isEmpty()) {
            return;
        }
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child(imageRef);
        storageRef.delete();
    }

    public static void addOutfitToDatabase(String userId, Outfit outfit, Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference outfitsRef = db.collection("user_clothes")
                                            .document(userId)
                                            .collection("outfits");
        Map<String,Object> outfitMap = parseOutfitToHashMap(outfit);


        outfitsRef.document(String.valueOf(outfit.getId())).set(outfitMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(context,"Outfit added sucessfully",Toast.LENGTH_SHORT).show();
                //user planned outfit by creating new one
                if(outfit.getDatesWorn() != null && !outfit.getDatesWorn().isEmpty()) {
                    logOutfit(userId, outfit, outfit.getDatesWorn().get(0),context);
                    //update percentiles
                    getUserClothingItemsByIds(userId, outfit.getClothingItemsIds(), new OnUserClothingItemsCallback() {
                        @Override
                        public void onUserClothingItemsLoaded(List<ClothingItem> clothingItems) {
                            for(ClothingItem clothingItem : clothingItems) {
                                updateClothingItemStatistics(userId, clothingItem);
                            }
                        }

                        @Override
                        public void onError(Exception e) {

                        }
                    });
                }
            }
        });
    }

    public static void isOutfitPlannedOnDate(String userId, String date, final OnCompleteListener<QuerySnapshot> listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Query query = db.collection("user_clothes")
                .document(userId)
                .collection("outfits")
                .whereArrayContains("datesWorn",date);
        query.get().addOnCompleteListener(listener);
    }

    public static void logOutfit(String userId, Outfit outfit, String date, Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference outfitRef = db.collection("user_clothes")
                                    .document(userId)
                                    .collection("outfits")
                                    .document(String.valueOf(outfit.getId()));
        //update datesWorn field of outfit
        outfitRef.update("datesWorn", FieldValue.arrayUnion(date)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(context,"Outfit logged successfully",Toast.LENGTH_SHORT).show();
            }
        });
        //update timesWorn field of clothing in outfit
        for(String clothingItemId : outfit.getClothingItems()) {
            wearClothingItem(userId,clothingItemId,date);
        }
    }

    public static void unlogOutfit(String userId, Outfit outfit, String date, Context context) {
        //update timesWorn field of clothing in outfit
        for (String clothingItemId : outfit.getClothingItems()) {
            unwearClothingItem(userId, clothingItemId, date);
        }


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference outfitRef = db.collection("user_clothes")
                .document(userId)
                .collection("outfits")
                .document(String.valueOf(outfit.getId()));
        if(outfit.getImageLink() == null || outfit.getImageLink().isEmpty() || outfit.getImageLink().equals("null")) {
            removeItemFromDatabase(userId,"outfits", String.valueOf(outfit.getId()));
        } else {
            //update datesWorn field of outfit
            outfitRef.update("datesWorn", FieldValue.arrayRemove(date)).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    Toast.makeText(context, "Outfit unlogged successfully", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    public static void updateClothingItemStatistics(String userId, ClothingItem clothingItem) {
        //update wearing percentile for clothing items of the same type

        //get all clothing of the same type
        Query query = FirebaseFirestore.getInstance().collection("user_clothes")
                .document(userId).collection("clothes").whereEqualTo("type",clothingItem.getType());
        getUserClothingItems(query, new OnUserClothingItemsCallback() {
            @Override
            public void onUserClothingItemsLoaded(List<ClothingItem> clothingItems) {
                //update percentiles
                List<Integer> percentiles = calculatePercentiles(clothingItems);
                int i = 0;
                for(ClothingItem item : clothingItems) {
                    updatePercentile(userId, String.valueOf(item.getId()),percentiles.get(i));
                    i++;
                }

            }
            @Override
            public void onError(Exception e) {
            }
        });
    }

    public static void updatePercentile(String userId, String clothingItemId, int percentile) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference clothingItemDocumentRef = db.collection("user_clothes")
                .document(userId).collection("clothes").document(clothingItemId);
        clothingItemDocumentRef.update("wearPercentile",percentile);
    }

    public static void wearClothingItem(String userId, String clothingItemId, String date) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference clothingItemDocumentRef = db.collection("user_clothes")
                .document(userId).collection("clothes").document(clothingItemId);

        LocalDate localDate = parseStringToLocalDate(date);
        String year = String.valueOf(localDate.getYear());
        String month = String.valueOf(localDate.getMonthValue());
        clothingItemDocumentRef.update("timesWornSeparated."+year+"."+month, FieldValue.increment(1));
        clothingItemDocumentRef.update("timesWorn",FieldValue.increment(1));
    }

    public static void unwearClothingItem(String userId, String clothingItemId, String date) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference clothingItemDocumentRef = db.collection("user_clothes")
                .document(userId).collection("clothes").document(clothingItemId);

        LocalDate localDate = parseStringToLocalDate(date);
        String year = String.valueOf(localDate.getYear());
        String month = String.valueOf(localDate.getMonthValue());
        clothingItemDocumentRef.update("timesWornSeparated."+year+"."+month, FieldValue.increment(-1));
        clothingItemDocumentRef.update("timesWorn",FieldValue.increment(-1));
    }


    public interface OnUserOutfitsCallback {
        void onUserOutfitsLoaded(List<Outfit> outfits);
        void onError(Exception e);
    }
    public static void getUserOutfits(Query query, OnUserOutfitsCallback callback) {
        List<Outfit> outfits = new ArrayList<>();

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    outfits.add(parseOutfit(document));
                }
                callback.onUserOutfitsLoaded(outfits);
            } else {
                callback.onError(task.getException());
            }
        });
    }


    public static void setUserPreferences(String userId, String colorSeason, String bodyType, String styles) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocumentRef = db.collection("user_clothes").document(userId);
        Map<String, Object> preferences = new HashMap<>();
        preferences.put("colorSeason",colorSeason);
        preferences.put("bodyType",bodyType);
        preferences.put("styles",styles);
        userDocumentRef.set(preferences);
    }

    public interface OnUserPreferencesCallback {
        void onUserPreferencesLoaded(HashMap<String,String> preferences);
        void onError(Exception e);
    }
    public static void getUserPreferences(String userId, OnUserPreferencesCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocumentRef = db.collection("user_clothes").document(userId);
        userDocumentRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                HashMap<String,String> preferences = new HashMap<>();
                preferences.put("colorSeason", String.valueOf(documentSnapshot.get("colorSeason")));
                preferences.put("bodyType", String.valueOf(documentSnapshot.get("bodyType")));
                preferences.put("styles", String.valueOf(documentSnapshot.get("styles")));
                callback.onUserPreferencesLoaded(preferences);
            }
        });
    }



    public static void addSwapItemToDatabase(String userId, SwapItem swapItem, Context context) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("swap").document(String.valueOf(swapItem.getSwapId()));
        HashMap<String, Object> swapItemMap = parseSwapItemToHashMap(swapItem);
        addClothingItemToDatabase(documentReference, swapItemMap, new AddClothingItemCallback() {
            @Override
            public void onClothingItemAdded() {
                Toast.makeText(context,"Clothing Item added to Swap successfully",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClothingItemAddFailed(Exception e) {
                Toast.makeText(context,"uh oh, something went wrong",Toast.LENGTH_SHORT).show();
            }
        });

        moveImageInFirebaseStorage(
                userId,
                parseToImageReference(userId, "clothes", String.valueOf(swapItem.getId())),
                "images/swap/"+swapItem.getSwapId(),
                String.valueOf(swapItem.getId()),
                String.valueOf(swapItem.getSwapId())
        );

    }


    public static void getSwapItems(String userId, OnUserClothingItemsCallback callback) {
        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("swap");
        List<ClothingItem> swapItems = new ArrayList<>();

        Query query = collectionReference.whereNotEqualTo("userId",userId).whereIn("acceptedRequest", Collections.singletonList(""));
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    SwapItem swapItem = document.toObject(SwapItem.class);
                    List<HashMap<String,String>> userRequests = (List<HashMap<String, String>>) document.get("userRequests");
                    HashMap<String,String> users = parseUserRequests(userRequests);
                    swapItem.setRequests(users);
                    swapItems.add(swapItem);
                }
                callback.onUserClothingItemsLoaded(swapItems);
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public static void getSwapItemsAcceptedRequest(String userId, OnUserClothingItemsCallback callback) {
        CollectionReference collectionReference = FirebaseFirestore.getInstance().collection("swap");
        List<ClothingItem> swapItems = new ArrayList<>();

        Query query = collectionReference.whereEqualTo("acceptedRequest",userId);
        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    SwapItem swapItem = document.toObject(SwapItem.class);
                    List<HashMap<String,String>> userRequests = (List<HashMap<String, String>>) document.get("userRequests");
                    HashMap<String,String> users = parseUserRequests(userRequests);
                    swapItem.setRequests(users);
                    swapItems.add(swapItem);
                }
                callback.onUserClothingItemsLoaded(swapItems);
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public static HashMap<String,String> parseUserRequests(List<HashMap<String,String>>  userRequests) {
        if(userRequests == null) {
            return null;
        }
        HashMap<String,String> users = new HashMap<>();
        for(HashMap<String,String> userInfo : userRequests) {
            for (Map.Entry<String, String> innerEntry : userInfo.entrySet()) {
                String id = innerEntry.getKey();
                String email = innerEntry.getValue();
                users.put(email,id);
            }
        }
        return users;
    }

    public static void getUserSwapItems(String userId, OnUserClothingItemsCallback callback) {
        Query query = FirebaseFirestore.getInstance().collection("swap")
                .whereEqualTo("userId",userId);
        List<ClothingItem> swapItems = new ArrayList<>();

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    SwapItem swapItem = document.toObject(SwapItem.class);
                    List<HashMap<String,String>> userRequests = (List<HashMap<String, String>>) document.get("userRequests");
                    HashMap<String,String> users = parseUserRequests(userRequests);
                    swapItem.setRequests(users);
                    swapItems.add(swapItem);
                }
                callback.onUserClothingItemsLoaded(swapItems);
            } else {
                callback.onError(task.getException());
            }
        });
    }

    public static void removeSwapItemFromDatabase(String userId, String swapItemId, String clothingItemId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("swap").document(swapItemId);
        documentReference.delete();
        moveImageInFirebaseStorage(
                userId,
                "images/swap/"+swapItemId,
                parseToImageReference(userId,"clothes", clothingItemId),
                clothingItemId,
                swapItemId);
    }

    public static void moveImageInFirebaseStorage(String userId, String currentPath, String newPath, String clothingItemId, String swapItemId) {
        StorageReference oldReference = FirebaseStorage.getInstance().getReference().child(currentPath);
        StorageReference newReference = FirebaseStorage.getInstance().getReference().child(newPath);

        oldReference.getBytes(Long.MAX_VALUE).addOnSuccessListener(bytes -> {
            newReference.putBytes(bytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    oldReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            newReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    updateSwapImageUrl(swapItemId, String.valueOf(task.getResult()));
                                    updateImageUrl(userId,"clothes",clothingItemId, String.valueOf(task.getResult()));
                                }
                            });

                        }
                    });
                }
            });
        });

    }


    public static void sendSwapRequest(Context context, String userId, String userEmail, String swapItemId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("swap").document(swapItemId);

        HashMap<String,String> userInfo = new HashMap<>();
        userInfo.put(userId,userEmail);
        documentReference.update("userRequests",FieldValue.arrayUnion(userInfo)).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Toast.makeText(context,"Request sent",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static void setAcceptedSwapRequest(String userId, String swapItemId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference documentReference = db.collection("swap").document(swapItemId);
        documentReference.update("acceptedRequest",userId);
    }
}

