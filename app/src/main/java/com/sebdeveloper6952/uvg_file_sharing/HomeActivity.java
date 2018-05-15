package com.sebdeveloper6952.uvg_file_sharing;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity
{
    // FIREBASE
    private FirebaseDatabase database;
    // OTHER
    protected List<String> coursesList;
    public static final String COURSE_NAME = "COURSE_NAME";
    // VIEWS
    protected ListView lViewCourses;
    protected ArrayAdapter<String> lViewCoursesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // INITIALIZE LIST
        coursesList = new ArrayList<>();

        // GET REFERENCE TO VIEWS
        prepareViews();
        // Get Course Data from Firebase
        getCourseData();
    }

    private void getCourseData()
    {
        // Read from the database
        ChildEventListener eventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try
                {
                    // POPULATE LIST OF COURSES, SO IT UPDATES THE COURSES LIST VIEW
                    coursesList.add((String)dataSnapshot.getKey());
                    lViewCoursesAdapter.notifyDataSetChanged();
                    Toast.makeText(HomeActivity.this,
                            "onChildAdded: ", Toast.LENGTH_LONG).show();
                }
                catch(ClassCastException ex)
                {
                    Toast.makeText(HomeActivity.this, "onChildAdded ERROR",
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Toast.makeText(HomeActivity.this,
                        "onChildChanged: ", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Toast.makeText(HomeActivity.this,
                        "onChildRemoved: ", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Toast.makeText(HomeActivity.this,
                        "onChildMoved: ", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(HomeActivity.this,
                        "onCancelled: ", Toast.LENGTH_LONG).show();
            }
        };

        // database testing
        database = FirebaseDatabase.getInstance();
        // reference to /materias/
        DatabaseReference matRef = database.getReference("materias");
        matRef.addChildEventListener(eventListener);
    }

    private void prepareViews()
    {
        lViewCourses = findViewById(R.id.lView_HomeActivity_Courses);
        // BIND ADAPTER TO COURSES LIST VIEW
        lViewCoursesAdapter = new ArrayAdapter<>(HomeActivity.this,
                android.R.layout.simple_list_item_1, coursesList);
        lViewCourses.setAdapter(lViewCoursesAdapter);
        lViewCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // start activity CourseFileDetails with course id as intent extra
                Intent intent = new Intent(HomeActivity.this, CourseFileDetails.class);
                intent.putExtra(COURSE_NAME, coursesList.get(position));
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });
    }
}
