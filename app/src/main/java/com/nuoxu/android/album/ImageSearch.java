package com.nuoxu.android.album;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ImageSearch extends AppCompatActivity{
    private SearchView imageSearchView;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private RecyclerView recyclerView;
    private DemoAdapter adapter;
    private ArrayList<String> imageUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_search);

        init();

        //Recycler view configuration
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.setAdapter(adapter = new DemoAdapter());

        imageSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                imageUrls.clear();
                adapter.replaceAll(imageUrls);
                adapter.notifyDataSetChanged();
                if(!newText.isEmpty()) {
                    CollectionReference colRef = FirebaseFirestore.getInstance().collection(user.getUid());
                    colRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    Log.d("Labels", document.getId() + " => " + document.getData());
                                    ArrayList<String> labelArray = (ArrayList)document.get("labels");
                                    String url = document.get("Url").toString();
                                    if(labelArray.toString().contains(newText) && !imageUrls.contains(url)){
                                        imageUrls.add(url);
                                    }
                                }
                                adapter.replaceAll(imageUrls);
                                adapter.notifyDataSetChanged();
                            } else {
                                Log.e("Keyword query","Unsuccessful");
                            }
                        }
                    });
                }
                return false;
            }
        });

        imageSearchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    private void init(){
        imageSearchView = (SearchView)findViewById(R.id.image_search_view);
        imageSearchView.setQueryHint("Try to type what's in the image");
        recyclerView = (RecyclerView) findViewById(R.id.search_recycler_view);
        imageUrls = new ArrayList<>();
    }
}