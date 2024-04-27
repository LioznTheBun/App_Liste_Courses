package com.medassi.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity2 extends AppCompatActivity {
    int idUtilisateur;
    ListView listCourses;
    ListView listCadis;
    ArrayList<String> rayons;
    private final Handler handler = new Handler();
    private final Runnable refreshRunnable = new Runnable() {
        @Override
        public void run() {
            // Actualiser la liste ici
            loadIntoListCourses();
            loadIntoListCadis();
            // Planifier une nouvelle actualisation après un certain délai
            handler.postDelayed(this, 6000); // Actualisation
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        // Démarrer la tâche périodique pour actualiser automatiquement la liste
        handler.postDelayed(refreshRunnable, 6000); // Actualisation
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Arrêter la tâche périodique pour éviter les fuites de mémoire
        handler.removeCallbacks(refreshRunnable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main2);
        rayons = new ArrayList<>();
        loadRayons();
        Button buttonNuke = findViewById(R.id.nuke);
        buttonNuke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteTable();
            }
        });
        Button buttonShowPopup = findViewById(R.id.buttonShowPopup);
        buttonShowPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        ((TextView)findViewById(R.id.textView)).setText(MainActivity.ragondin);
        idUtilisateur = MainActivity.idUtilisateur ;
        listCourses = findViewById(R.id.listCourse);
        listCadis = findViewById(R.id.listCadis);
        loadIntoListCourses();
        loadIntoListCadis();
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void deleteTable() {
        String url = "http://sio.jbdelasalle.com/~epourchon/courses/index.php?action=suppr";

        // Création de la requête Volley
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray jsonArray = response.getJSONArray("result");
                        Toast.makeText(this, "Suppression réussi", Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur lors du parsing des données", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Gérer l'erreur
                    Toast.makeText(this, "Erreur lors de la suppression", Toast.LENGTH_SHORT).show();
                });

        // Ajout de la requête à la file d'attente de Volley
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void openCustomDialog(String nom, String rayon, String id) {
        // Récupérer le layout XML personnalisé
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.listview_popup_layout, null);

        // Récupérer les TextView dans le layout
        TextView nomCoursesTextView = dialogView.findViewById(R.id.nom_courses);
        TextView commentaireTextView = dialogView.findViewById(R.id.commentaire);

        // Remplacer les textes des TextView si nécessaire
        nomCoursesTextView.setText(nom);
        commentaireTextView.setText(rayon);

        // Créer une boîte de dialogue personnalisée
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        JSONObject produit = new JSONObject();
                        try {
                            produit.put("id_produit", id);
                            produit.put("utilisateur", idUtilisateur);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mettreDansLeCadis(produit);
                    }
                })
                .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        // Afficher la boîte de dialogue
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDialog() {
        final Dialog dialog = new Dialog(this);
        // Nous définissons le layout de notre custom dialog
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_layout);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screenWidth = displayMetrics.widthPixels;
        int screenHeight = displayMetrics.heightPixels;
        dialog.getWindow().setLayout((int) (screenWidth * 0.8), (int) (screenHeight * 0.6));

        EditText editTextName = dialog.findViewById(R.id.editTextName);
        EditText editTextComment = dialog.findViewById(R.id.editTextComment);
        Button buttonSubmit = dialog.findViewById(R.id.buttonSubmit);
        Spinner spinnerRadius = dialog.findViewById(R.id.spinner);
        List<String> radiusOptions = new ArrayList<>();
        loadRayonsIntoSpinner(spinnerRadius);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, radiusOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRadius.setAdapter(adapter);
        spinnerRadius.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Empêcher la sélection de la première option
                if (position == 0) {
                    // Ici, vous pouvez afficher un message d'erreur ou simplement ignorer la sélection
                    Toast.makeText(MainActivity2.this, "Veuillez sélectionner un rayon valide.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Ce code peut rester vide, nécessaire pour implémenter l'interface
            }
        });


        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nom = editTextName.getText().toString();
                String commentaire = editTextComment.getText().toString();
                int rayonId = spinnerRadius.getSelectedItemPosition();

                // Créer un objet JSON avec les données
                JSONObject produit = new JSONObject();
                try {
                    produit.put("nom", nom);
                    produit.put("commentaire", commentaire);
                    produit.put("auteurMiseEnList", idUtilisateur);
                    produit.put("rayon_id", rayonId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Envoyer le JSON au serveur
                envoyerProduitAuServeur(produit);
                dialog.dismiss(); // Ferme la popup après la soumission
            }
        });

        dialog.show();
    }

    private void mettreDansLeCadis(JSONObject produit) {
        String url = "http://sio.jbdelasalle.com/~epourchon/courses/index.php?action=transferer"; // Remplacez par l'URL de votre serveur

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, produit,
                response -> {
                    // Réponse reçue du serveur
                    try {
                        boolean result = response.getBoolean("result");
                        if (result) {
                            Toast.makeText(MainActivity2.this, "Produit ajouté avec succès", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity2.this, "Erreur lors de l'ajout du produit", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Gestion des erreurs de réseau
                    Toast.makeText(MainActivity2.this, "Erreur de connexion au serveur", Toast.LENGTH_SHORT).show();
                });

        // Ajouter la requête à la file d'attente de Volley
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
    private void envoyerProduitAuServeur(JSONObject produit) {
        String url = "http://sio.jbdelasalle.com/~epourchon/courses/index.php?action=ajouter"; // Remplacez par l'URL de votre serveur

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, produit,
                response -> {
                    // Réponse reçue du serveur
                    try {
                        boolean result = response.getBoolean("result");
                        if (result) {
                            Toast.makeText(MainActivity2.this, "Produit ajouté avec succès", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity2.this, "Erreur lors de l'ajout du produit", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Gestion des erreurs de réseau
                    Toast.makeText(MainActivity2.this, "Erreur de connexion au serveur", Toast.LENGTH_SHORT).show();
                });

        // Ajouter la requête à la file d'attente de Volley
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void loadRayons() {
        String url = "http://sio.jbdelasalle.com/~epourchon/courses/index.php?action=rayons";

        // Préparer la liste avec la valeur par défaut
        rayons.add("Sélectionnez un rayon...");

        // Création de la requête Volley
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Extraire le tableau JSON 'result'
                        JSONArray jsonArray = response.getJSONArray("result");

                        // Parcourir le tableau et ajouter chaque rayon à la liste
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String nomRayon = jsonObject.getString("nom"); // Utiliser "nom" pour le nom du rayon
                            rayons.add(nomRayon);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur lors du parsing des données", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Gérer l'erreur
                    Toast.makeText(this, "Erreur de chargement des rayons", Toast.LENGTH_SHORT).show();
                });

        // Ajout de la requête à la file d'attente de Volley
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void loadRayonsIntoSpinner(final Spinner spinner) {
        String url = "http://sio.jbdelasalle.com/~epourchon/courses/index.php?action=rayons";

        // Préparer la liste avec la valeur par défaut
        List<String> listRayons = new ArrayList<>();
        listRayons.add("Sélectionnez un rayon...");

        // Création de la requête Volley
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // Extraire le tableau JSON 'result'
                        JSONArray jsonArray = response.getJSONArray("result");

                        // Parcourir le tableau et ajouter chaque rayon à la liste
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String nomRayon = jsonObject.getString("nom"); // Utiliser "nom" pour le nom du rayon
                            listRayons.add(nomRayon);
                        }

                        // Mise à jour de l'adapter du Spinner
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listRayons);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinner.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Erreur lors du parsing des données", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    // Gérer l'erreur
                    Toast.makeText(this, "Erreur de chargement des rayons", Toast.LENGTH_SHORT).show();
                });

        // Ajout de la requête à la file d'attente de Volley
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
    private void loadIntoListCourses() {
        String url = "http://sio.jbdelasalle.com/~epourchon/courses/index.php?action=courses";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Analyser le tableau JSON 'result'
                            JSONArray courses = response.getJSONArray("result");
                            ArrayList<HashMap<String, String>> courseNames = new ArrayList<HashMap<String, String>>();
                            ArrayList<Integer> productIds = new ArrayList<>();
                            HashMap<String, String> item;

                            for (int i = 0; i < courses.length(); i++) {
                                item = new HashMap<String, String>();
                                JSONObject course = courses.getJSONObject(i);
                                int id = course.getInt("id");
                                String nom = course.getString("nom");
                                String commentaire = course.getString("commentaire");
                                // Ajouter les données extraites à une liste ou à un adaptateur
                                if(commentaire.equals("")) {
                                    item.put("nom" , nom);
                                    item.put("rayon", rayons.get(course.getInt("rayon_id")));
                                    item.put("id", String.valueOf(id));
                                } else {
                                    item.put("nom" , nom + " - " + commentaire);
                                    item.put("rayon", rayons.get(course.getInt("rayon_id")));
                                    item.put("id", String.valueOf(id));
                                }
                                courseNames.add(item);
                            }

                            //Création d'un SimpleAdapter qui se chargera de mettre les items présents dans notre list (listItem) dans la vue affichageitem
                            SimpleAdapter mSchedule = new SimpleAdapter (MainActivity2.this, courseNames, R.layout.custom_layout,
                                    new String[] {"nom", "rayon"}, new int[] {R.id.course_name, R.id.rayon_nom});

                            //On attribue à notre listView l'adapter que l'on vient de créer
                            listCourses.setAdapter(mSchedule);

                            listCourses.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                @SuppressWarnings("unchecked")
                                public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                                    HashMap<String, String> map = (HashMap<String, String>) listCourses.getItemAtPosition(position);
                                    openCustomDialog(map.get("nom"), map.get("rayon") ,map.get("id"));
                                }
                            });

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity2.this, "Erreur de parsing JSON", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Gérer l'erreur
                Toast.makeText(MainActivity2.this, "Erreur de réseau", Toast.LENGTH_SHORT).show();
            }
        });

        // Ajouter la requête à la file d'attente de requêtes Volley
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    private void loadIntoListCadis() {
        String url = "http://sio.jbdelasalle.com/~epourchon/courses/index.php?action=cadis";

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Analyser le tableau JSON 'result'
                            JSONArray courses = response.getJSONArray("result");
                            ArrayList<String> courseNames = new ArrayList<>();

                            for (int i = 0; i < courses.length(); i++) {
                                JSONObject course = courses.getJSONObject(i);
                                String nom = course.getString("nom");
                                String commentaire = course.getString("commentaire");
                                // Ajouter les données extraites à une liste ou à un adaptateur
                                if(commentaire.equals("")) {
                                    courseNames.add(nom + " - " + rayons.get(course.getInt("rayon_id")));
                                } else {
                                    courseNames.add(nom + " - " + rayons.get(course.getInt("rayon_id")) + " - " + commentaire);
                                }
                            }

                            // Ici, mettez à jour votre ListView ou autre vue avec les données
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity2.this,
                                    android.R.layout.simple_list_item_1, courseNames);
                            listCadis.setAdapter(adapter);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity2.this, "Erreur de parsing JSON", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Gérer l'erreur
                Toast.makeText(MainActivity2.this, "Erreur de réseau", Toast.LENGTH_SHORT).show();
            }
        });

        // Ajouter la requête à la file d'attente de requêtes Volley
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }


}