package com.estevan.quizapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.estevan.quizapp.Adapters.QuestionsAdapter;
import com.estevan.quizapp.Models.QuestionModel;
import com.estevan.quizapp.databinding.ActivityQuestionBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class QuestionActivity extends AppCompatActivity {

    ActivityQuestionBinding binding;
    FirebaseDatabase database;
    ArrayList<QuestionModel>list;
    QuestionsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQuestionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();

        database = FirebaseDatabase.getInstance();

        list = new ArrayList<>();

        int setNum = getIntent().getIntExtra("setNum", 0);
        String categoryName = getIntent().getStringExtra("categoryName");

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyQuestions.setLayoutManager(layoutManager);

        adapter = new QuestionsAdapter(this, list, categoryName, new QuestionsAdapter.DeleteListener() {
            @Override
            public void onLongClick(int position, String id) {

                AlertDialog.Builder builder = new AlertDialog.Builder(QuestionActivity.this);
                builder.setTitle("Eliminar pregunta");
                builder.setMessage("Estas seguro, que quieres eliminar esta pregunta");

                builder.setPositiveButton("Si",(dialogInterface, i) -> {

                    database.getReference().child("Sets").child(categoryName).child("questions")
                            .child(id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {

                                    Toast.makeText(QuestionActivity.this, "Pregunta eliminada", Toast.LENGTH_SHORT).show();

                                }
                            });

                });

                builder.setNegativeButton("No", (dialogInterface, i) -> {

                    dialogInterface.dismiss();

                });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();

            }
        });
        binding.recyQuestions.setAdapter(adapter);

        database.getReference().child("Sets").child(categoryName).child("questions")
                        .orderByChild("setNum").equalTo(setNum)
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                if (snapshot.exists()){

                                    list.clear();

                                    for (DataSnapshot dataSnapshot:snapshot.getChildren()){

                                        QuestionModel model = dataSnapshot.getValue(QuestionModel.class);
                                        model.setKey(dataSnapshot.getKey());
                                        list.add(model);

                                    }

                                    adapter.notifyDataSetChanged();

                                }
                                else {

                                    Toast.makeText(QuestionActivity.this, "las preguntas no existen", Toast.LENGTH_SHORT).show();

                                }

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

        binding.addQuestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(QuestionActivity.this, AddQuestionActivity.class);
                intent.putExtra("category",categoryName);
                intent.putExtra("setNum",setNum);
                startActivity(intent);
            }
        });

    }
}