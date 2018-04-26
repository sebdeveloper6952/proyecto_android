package com.sebdeveloper6952.uvg_file_sharing;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class HomeActivity extends AppCompatActivity
{
    // declare storage reference
    private StorageReference mStorageRef;
    // views
    private Button btnUpload;
    private Button btnChooseImg;
    private TextView txtViewInfo;
    // URI of the selected image
    private Uri selImgUri;
    // constants
    private final int RC_WRITE_IMG_CODE = 0;
    private final int RC_CHOOSE_IMG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        // initialize storage reference
        mStorageRef = FirebaseStorage.getInstance().getReference();
        btnUpload = findViewById(R.id.btn_Upload);
        btnChooseImg = findViewById(R.id.btn_pickImg);
        txtViewInfo = findViewById(R.id.txtView_Info);
        prepareViews();
        selImgUri = null;
    }

    /**
     * Method that handles results of activities started for results, such as uploading
     * an image.
     * @param requestCode
     * @param resultCode
     * @param resultData
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData)
    {
        // revisar requestCode y resultCode para determinar a que actividad se responde
        if (requestCode == RC_CHOOSE_IMG)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                if (resultData != null)
                {
                    selImgUri = resultData.getData();
                    // TODO: (mejorar) mostrar al usuario el archivo que subira
                    txtViewInfo.setText(getFileName(selImgUri) + "\n");
                    Toast.makeText(getApplicationContext(), R.string.msg_img_selected_success,
                            Toast.LENGTH_SHORT).show();
                    // activar boton para subir imagen
                    btnUpload.setEnabled(true);
                }
            }
            else
            {
                // TODO: (mostrar algun feedback) error en operacion de subir imagen
                Toast.makeText(getApplicationContext(), R.string.msg_img_selected_failed,
                        Toast.LENGTH_SHORT).show();
                selImgUri = null;
                btnUpload.setEnabled(false);
            }
        }
    }

    /**
     * Sube una imagen, por ahora la sube al folder img/
     */
    private void uploadImage()
    {
        try
        {
            if(selImgUri != null)
            {
                StorageReference stRef = mStorageRef.child("img/" + getFileName(selImgUri));
                final UploadTask task = stRef.putFile(selImgUri);
                task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        Toast.makeText(getApplicationContext(), R.string.msg_img_upload_successful,
                                Toast.LENGTH_SHORT).show();
                        // resetear imagen seleccionada
                        // TODO: ver si se mete en metodo
                        selImgUri = null;
                        btnChooseImg.setEnabled(true);
                        btnUpload.setEnabled(false);
                    }
                });
                task.addOnFailureListener(new OnFailureListener()
                {
                    @Override
                    public void onFailure(@NonNull Exception e)
                    {
                        Toast.makeText(getApplicationContext(), R.string.msg_img_upload_failed,
                                Toast.LENGTH_SHORT).show();
                        btnChooseImg.setEnabled(true);
                        btnUpload.setEnabled(true);
                    }
                });
                task.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
                    {
                        long bytes = taskSnapshot.getBytesTransferred();
                        txtViewInfo.append("Uploading: " + bytes + "\n");
                    }
                });
            }
        }
        catch(Exception e)
        {
            txtViewInfo.setText(e.getMessage());
        }
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
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
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

    /**
     *  Cierra la sesion del usuario.
     */
    protected void userLogout()
    {
        AuthUI.getInstance().signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        // TODO: return to login screen
                        Toast.makeText(getApplicationContext(), R.string.msg_img_upload_failed,
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     *  Inicializacion de Views.
     */
    protected void prepareViews()
    {
        // boton para subir imagen comienza desactivado
        btnUpload.setEnabled(false);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                uploadImage();
                btnChooseImg.setEnabled(false);
                btnUpload.setEnabled(false);
            }
        });

        btnChooseImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, RC_CHOOSE_IMG);
            }
        });
    }
}
