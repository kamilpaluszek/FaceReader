package com.example.facereader;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.MultipartBody;


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executors;

//glowna klasa naszej aplikacji
public class MainActivity extends AppCompatActivity {

    //elementy ekranu glównego
    ImageView imageView;
    Button btnGallery, btnTakeaPhoto;
    TextView textView;

    //zmienne aparatu
    int TAKE_PICTURE_CODE = 100, REQUEST_PERMISSION_CODE = 101, MAKE_PHOTO_CODE = 0;

    //zmienna potrzebna do otworzenia wybranego zdjecia z galerii
    Uri imageUri;

    //nasza bitmapa na ekranie glownym
    Bitmap mBitmap;

    //funkcja, która ma za zadanie wyświetlić pytanie dla użytkownika o zezwolenie na dostęp do aparatu podczas użytkowania aplikacji
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
        }

    }

    //funkcja odpowiedzialna za rozstawienie elementow/dostęp do funkcji telefonu użytkownika
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //ustawienie widoku
        setContentView(R.layout.activity_main);

        //ustawienie toolbaru
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //zezwolenie użytkownika na dostęp do pamięci telefonu
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WRITE_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.INTERNET
            }, REQUEST_PERMISSION_CODE);
        }

        //inicjalizacja klasy ContentResult - potrafiaca pomiescic kilka wartosci
        ContentValues values = new ContentValues();

        //wartosci potrzebne do zrobienia zdjęcia kamerą oraz aktualizacji na ekranie
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        //rozmieszczenie elementów
        initViews();
    }

    //funkcja odpowiedzialna za rozmieszczenie przyciskow, textView oraz imageView i przypisania do nich funkcji obsługi
    private void initViews() {
        btnGallery = (Button) findViewById(R.id.btnGallery);
        btnTakeaPhoto = (Button) findViewById(R.id.btnTakeaPhoto);
        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.textView);

        //obsługa kliknięcia przycisku TakePicture (wybór zdjęcia z galerii)
        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //wywołanie funkcji wyboru zdjęcia z galerii
                takePicFromGallery();
            }
        });

        //obsługa kliknięcia przycisku MakePhoto (zrobienie zdjęcia kamera przednią/tylnią)
        btnTakeaPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //stworzenie Intencji, która pozwala na ustawienie akcji powiązanej z zrobieniem zdjęcia
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

                //zaczynamy aktywność zrobienia zdjecia z zmienna MAKE_PHOTO_CODE.
                //nastepnie kod tej aktywnosci zostanie obsluzony w funckji onActivityResult
                startActivityForResult(intent, MAKE_PHOTO_CODE);
            }

        });
    }

    //Stworzenie menu glownego okna z pliku menu.xml
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    //funkcja obslugi wyboru opcji menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //klikniecie opcji "About" rozpocznie tworzenie klasy AboutActivity
            case R.id.about:
                Intent about = new Intent(this, AboutActivity.class);
                startActivity(about);
                return true;

            //klikniecie opcji "Licence" rozpocznie tworzenie klasy LicenceActivity
            case R.id.licence:
                Intent licence = new Intent(this, PhotosActivity.class);
                startActivity(licence);
                //finish();
                return true;

            //klikniecie opcji "Exit" spowoduje wyjście z aplikacji
            case R.id.exit:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    //funkcja obslugujaca zamiane zdjęcia na tablicę bajtów oraz równoległe wysłanie zdjęcia w tej formie na serwer wykorzystująć Async Task
    private void processImage() {
        //zainicjalizowanie klas strumieni (zdjecie bedziemy wysylac w formie tablicy bajtów)
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        //skompresowanie Bitmapy na strumień
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        //zamiana strumienia na tablicę bajtów
        byte[] byteArray = stream.toByteArray();

        AsyncTask<byte[], String, Void> processAsync = new AsyncTask <byte[], String, Void >() {

            //klasa ProgressDialog pozwoli poinformować użytkownika o wykonywaniu jakiejś operacji przez program podczas operacji
            ProgressDialog mDialog = new ProgressDialog(MainActivity.this);

            //ta funkcja zostanie wykonana przed wywołaniem  AsyncTask
            @Override
            protected void onPreExecute() {
                mDialog.show();
            }

            //funkcja będzie wykonywana podczas postępów AsyncTask
            @Override
            protected void onProgressUpdate(String... values) {
                mDialog.setMessage(values[0]);
            }

            //funkcja, która działa na osobnym wątku, nie obciążając tylko jednego obliczeniami. Dzięki temu w tym samym czasie połączymy się z serwerem
            @Override
            protected Void doInBackground (byte[]... byteArray){    //String
                //String responseString = null;

                connectServer(byteArray[0]);
                    //postRequest("http://192.168.1.105:5000/test/", "Hello");
                  //return responseString;
                return null;
                }



            protected void onPostExecute(Void result){
                mDialog.dismiss();
              //  textView.setText(result);


            }
        };
        processAsync.execute(byteArray);

    }

    //funkcja służąca do obrócenia zdjecia, gdy jest wybrany z galerii. Funkcja sprawdza w jakim położeniu jest zdjęcie, jeśli w złym - to je zmienia
    private Bitmap rotateImage(Bitmap bitmap){
        ExifInterface exifInterface =null;

        try{

            exifInterface = new ExifInterface(getRealPathFromURI(imageUri));
        }catch(IOException e){
            e.printStackTrace();
        }
        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        Matrix matrix = new Matrix();

        //Sprawdzenie orientacji oraz zmiana jej w przypadku zlej orientacji zdjecia
        switch(orientation){
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(270);
                break;
            default:
        }

        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        return rotatedBitmap;
    }




    //funkcja sluzaca do polaczenia sie z naszym serwerem lokalnym
   void connectServer(byte[]... byteArray){
        //nasz url na ktory wyslemy zapytanie POST
        String postUrl= "http://192.168.1.103:5000/classifier/run";

       //wysylamy tablice bajtow, wiec wybieramy text/plain jako metode media
        MediaType mediaType = MediaType.parse("text/plain; charset=utf-8");

        //stworzenie postBodyImage z potrzebnymi parametrami
        RequestBody postBodyImage = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", "androidFlask.jpg", RequestBody.create(MediaType.parse("image/*jpg"), byteArray[0]))
                .build();

        postRequest(postUrl, postBodyImage);
    }

    //funkcja, która posiada nasza metode POST odpowiednio zmodyfikowaną pod polaczenie z naszym serwerem
    void postRequest(String postUrl, RequestBody postBody) {
        //do polaczenia z serwerem uzyjemy biblioteki OkHttpClient
        OkHttpClient client = new OkHttpClient();
        //stworzenie POST request za pomoca buildera
        Request request = new Request.Builder()
                .url(postUrl)
                .post(postBody)
                .build();
        //wykonanie POST request
        client.newCall(request).enqueue(new Callback() {

            //funkcja służąca do obsługi przeslania metody POST.
            @Override
            public void onFailure(Call call, IOException e) {
                //Anulowanie połączenia
                call.cancel();

                //Przy pomocy openDialog informujemy użytkownika o błędzie dotyczącym połączenia z serverem
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        openDialog("Failed connecting to server!");
                    }
                });
            }

            //funkcja wykonywana po wyslaniu odpowiedzi od serwera
            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                //odczyt danych z serwera
                Executors.newSingleThreadExecutor().submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //stworzenie tablicy json w celu wyluskania poszczegolnych danych w petli
                            JSONArray jsonArray = new JSONArray(response.body().string());
                            //dlugosc tablicy
                            int jsonLength = jsonArray.length();

                            //odczyt pierwszej predykcji - w przypadku parametru "Unable" - obsługa błędu
                            JSONObject jsonObje = jsonArray.getJSONObject(0);

                            if(jsonObje.getString("prediction").equals("Unable")){
                                openDialog("Unable to identify image!");
                            }

                            //odczyt danych oraz naniesienie ich na bitmapę
                            else {
                                String[] emotion = new String[jsonLength];
                                String[] xrect = new String[jsonLength];
                                String[] yrect = new String[jsonLength];
                                String[] widthrect = new String[jsonLength];
                                String[] heighrect = new String[jsonLength];


                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObj = jsonArray.getJSONObject(i);
                                    emotion[i] = jsonObj.getString("prediction");
                                    xrect[i] = jsonObj.getString("xrect");
                                    yrect[i] = jsonObj.getString("yrect");
                                    widthrect[i] = jsonObj.getString("wrect");
                                    heighrect[i] = jsonObj.getString("hrect");

                                }
                                imageView.setImageBitmap(ImageHelper.drawRectOnBitmap(mBitmap, jsonLength, xrect, yrect, heighrect, widthrect, emotion));
                            }
                            //obsługa błędu JSON oraz Exception
                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });
    }

    //funkcja tworząca instancje klasy ErrorDialog, która informuje uzytkownika o bledach polaczeniowych oraz niezidentyfikowania zdjęcia
    public void openDialog(String title) {
        ErrorDialog exampleDialog = ErrorDialog.newInstance(title);
        exampleDialog.show(getSupportFragmentManager(), "example dialog");
    }


    //funkcja obsługująca requestCode
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //jeśli funkcja startOnActivity przekaze kod wybrania zdjecia z galerii, nastepuje odczyt strumienia danych z wybranego
        //zdjecia z galerii przy pomocy Uri
        if (requestCode == TAKE_PICTURE_CODE && resultCode == RESULT_OK ) {
            //odczyt uri z wybranego zdjecia
            Uri selectedImageUri = data.getData();
            InputStream in = null;
            try {
                //odczyt zdjecia za pomoca strumienia danych
                in = getContentResolver().openInputStream(selectedImageUri);
                //obsluga bledu (nie znalezienie pliku)
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            //naniesienie na nasz ekran otrzymanego zdjecia w jak najlepszej jakosci oraz zeskalowanie go do ekranu, zmniejszenie
            //także rozmiaru zdjęcia, co pozwala na szybsze działanie aplikacji
            mBitmap = BitmapFactory.decodeStream(in);
            mBitmap=ImageHelper.scaleDown(mBitmap, 1000, true);
            imageView.setImageBitmap(mBitmap);
            //zamiana tego zdjęcia na tablicę bajtów w celu wysłaniu do serwera
            processImage();
        }
        //jeśli funkcja startOnActivity przekaze kod robienia zdjecia aparatem, nastepuje odczyt strumienia danych ze zrobionego
        //zdjecia przy pomocy Uri
        else if(requestCode == MAKE_PHOTO_CODE && resultCode == RESULT_OK ) {
            InputStream in = null;
                try {
                    mBitmap = MediaStore.Images.Media.getBitmap(
                            getContentResolver(), imageUri);
                    in = getContentResolver().openInputStream(imageUri);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //naniesienie na nasz ekran otrzymanego zdjecia w jak najlepszej jakosci oraz zeskalowanie go do ekranu, zmniejszenie
                //także rozmiaru zdjęcia, co pozwala na szybsze działanie aplikacji
                mBitmap = BitmapFactory.decodeStream(in);
                mBitmap=ImageHelper.scaleDown(mBitmap, 1000, true);
                mBitmap = rotateImage(mBitmap);
                imageView.setImageBitmap(mBitmap);
                //zamiana tego zdjęcia na tablicę bajtów w celu wysłaniu do serwera
                processImage();
            }
        }

        //funkcja, która pozwala nam na pobranie sciezki z wybranego zdjecia z telefonu
        public String getRealPathFromURI(Uri contentUri) {
            String[] proj = { MediaStore.Images.Media.DATA };
            Cursor cursor = managedQuery(contentUri, proj, null, null, null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }

        //funkcja, ktora obsluguje wybranie zdjecia z galerii
        private void takePicFromGallery(){
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, TAKE_PICTURE_CODE);
        }

}

