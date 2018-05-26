package com.sebdeveloper6952.uvg_file_sharing;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CourseFileDetails extends AppCompatActivity {

    // FIREBASE
    protected FirebaseDatabase database;
    protected StorageReference storage;
    // OTHER
    protected String courseName;
    protected int coursePosition;
    protected List<String> filesList;
    // URI of the selected image
    private Uri selImgUri;
    private String selImgName;
    // VIEWS
    protected ListView lVFiles;
    protected Button btnUpload;
    protected ArrayAdapter<String> lVFilesAdapter;
    //  ON ACTIVITY RESULT REQUEST CODES
    protected final int RC_CHOOSE_IMG = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_file_details);
        courseName = getIntent().getStringExtra(HomeActivity.COURSE_NAME);
        // get firebase references
        coursePosition = getIntent().getIntExtra("position", -1);
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance().getReference();
        filesList = new ArrayList<>();
        prepareViews();
        getFilesFromDatabase();
    }

    /**
     * Method that handles results of activities started for results, such as uploading
     * an image.
     * @param requestCode
     * @param resultCode
     * @param resultData
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // revisar requestCode y resultCode para determinar a que actividad se responde
        if (requestCode == RC_CHOOSE_IMG) {
            if (resultCode == Activity.RESULT_OK) {
                if (resultData != null) {
                    selImgUri = resultData.getData();
                    Toast.makeText(getApplicationContext(), R.string.msg_img_selected_success,
                            Toast.LENGTH_SHORT).show();
                    // activar boton para subir imagen
                    btnUpload.setEnabled(true);
                    // subir imagen seleccionada
                    uploadImage();
                }
            } else {
                // TODO: (mostrar algun feedback) error en operacion de subir imagen
                Toast.makeText(getApplicationContext(), R.string.msg_img_selected_failed,
                        Toast.LENGTH_SHORT).show();
                selImgUri = null;
                btnUpload.setEnabled(true);
            }
        }
    }

    private void uploadImage()
    {
        try
        {
            if(selImgUri != null)
            {
                selImgName = getFileName(selImgUri);
                StorageReference stRef = storage
                        .child("materias/" + courseName + "/" + selImgName);
                final UploadTask task = stRef.putFile(selImgUri);
                task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        Toast.makeText(getApplicationContext(), R.string.msg_img_upload_successful,
                                Toast.LENGTH_SHORT).show();
                        // agregar nombre de archivo a database
                        database.getReference("materias").child(courseName)
                                .push().setValue(selImgName);
                        // resetear imagen seleccionada
                        selImgUri = null;
                        selImgName = null;
                        btnUpload.setEnabled(true);
                    }
                });
                task.addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(getApplicationContext(), R.string.msg_img_upload_failed,
                                Toast.LENGTH_SHORT).show();
                        btnUpload.setEnabled(true);
                    }
                });
            }
        }
        catch(Exception e) { Log.i("CourseFileDetails:", e.getMessage()); }
    }

    /**
     * Metodo para obtener el nombre de un archivo a partir de un URI. Tomado del sitio oficial de
     * desarrolladores android.
     * @param uri
     * @return
     */
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null,
                    null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private void downloadFile(String name) {
        requestPermissions();
        StorageReference ref = storage.child("materias/" + courseName + "/" + name);
        lVFiles.setEnabled(false);
        btnUpload.setEnabled(false);
        try {
            Toast.makeText(CourseFileDetails.this, "Descargando...",
                    Toast.LENGTH_LONG).show();
            File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            String[] fileParts = name.split("\\.");
            if(fileParts.length < 2)
            {
                Toast.makeText(CourseFileDetails.this, "Error en archivo.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            File tempFile = File.createTempFile(fileParts[0], ".jpg", dir);
            ref.getFile(tempFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(CourseFileDetails.this, "Archivo descargado.",
                            Toast.LENGTH_LONG).show();
                    lVFiles.setEnabled(true);
                    btnUpload.setEnabled(true);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CourseFileDetails.this, "Descarga fallida.",
                            Toast.LENGTH_LONG).show();
                    lVFiles.setEnabled(true);
                    btnUpload.setEnabled(true);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(CourseFileDetails.this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(CourseFileDetails.this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(CourseFileDetails.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
    }

    private void prepareViews()
    {
        lVFiles = findViewById(R.id.lViews_CourseFilesDetails_Details);
        lVFilesAdapter = new ArrayAdapter<>(CourseFileDetails.this,
                android.R.layout.simple_list_item_1, filesList);
        lVFiles.setAdapter(lVFilesAdapter);

        lVFiles.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                // download file
                downloadFile(filesList.get(position));
                return true;
            }
        });
        btnUpload = findViewById(R.id.btn_CourseFileDetails_Upload);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),
                        RC_CHOOSE_IMG);
            }
        });
    }

    private void getFilesFromDatabase()
    {
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                // POPULATE LIST OF COURSES, SO IT UPDATES THE COURSES LIST VIEW
                filesList.add((String)dataSnapshot.getValue());
                lVFilesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // POPULATE LIST OF COURSES, SO IT UPDATES THE COURSES LIST VIEW
                filesList.add((String)dataSnapshot.getValue());
                lVFilesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        DatabaseReference courseRef = database.getReference("materias").child(courseName);
        courseRef.addChildEventListener(childEventListener);
    }
}
