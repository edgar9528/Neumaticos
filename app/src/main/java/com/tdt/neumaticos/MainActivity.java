package com.tdt.neumaticos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.tdt.neumaticos.Adapter.CustomExpandableListAdapter;
import com.tdt.neumaticos.Adapter.DrawerListAdapter;
import com.tdt.neumaticos.Fragments.FragmentAmigos;
import com.tdt.neumaticos.Helper.FragmentNavigationManager;
import com.tdt.neumaticos.Interface.NavigationManager;
import com.tdt.neumaticos.Model.DrawerItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mActivityTitle;
    private String[] items;

    //private ExpandableListView expandableListView;
    private ExpandableListAdapter adapter;
    private List<String> lstTitle;
    private Map<String,List<String>> lstChild;
    private NavigationManager navigationManager;

    ArrayList<String> nombresMenu;
    ArrayList<DrawerItem> elemenosMenu;
    String tituloActivo="";

    private ListView listView;

    String usuario,contraseña;
    int permisos;

    @Override
    public void onBackPressed() {

        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
        dialogo1.setTitle("Importante");
        dialogo1.setMessage("¿Desea salir de la app?");
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {

                Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
                startActivity(intent);
                finish();

            }
        });
        dialogo1.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {
                //cancelar();
            }
        });
        dialogo1.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        obtenerUsuario();

        //init view
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActivityTitle= getTitle().toString();
        listView= findViewById(R.id.navList);
        navigationManager= FragmentNavigationManager.getmInstance(this);


        View listHeaderView = getLayoutInflater().inflate(R.layout.nav_header,null,false);
        listView.addHeaderView(listHeaderView);

        TextView textView = findViewById(R.id.textViewNombre);
        textView.setText(usuario);

        addDrawersItem();
        setupDrawer();

        if(savedInstanceState== null)
        {
            selectItem(0,elemenosMenu.get(0).getName());
        }


    }


    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void selectFirsItemAsDefault() {
        if(navigationManager!= null)
        {
            String firtsItem = "Categorías|Principal";
            navigationManager.showFragment(firtsItem);
            getSupportActionBar().setTitle(firtsItem);
        }
    }

    private void addDrawersItem() {

        elemenosMenu = new ArrayList<DrawerItem>();
        nombresMenu = new ArrayList<>();
        nombresMenu.add("ejemplo1");
        nombresMenu.add("ejemplo2");

        elemenosMenu.add(new DrawerItem(nombresMenu.get(0), R.drawable.logotdt));
        elemenosMenu.add(new DrawerItem(nombresMenu.get(1), R.drawable.logotdt));

        listView.setAdapter(new DrawerListAdapter(this, elemenosMenu));

        listView.setOnItemClickListener(new DrawerItemClickListener());

    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close)
        {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle("Menú");
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                getSupportActionBar().setTitle(tituloActivo);
                invalidateOptionsMenu();
            }
        };

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            // Toma los eventos de selección del toggle aquí
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /* La escucha del ListView en el Drawer */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String titulo = elemenosMenu.get(position-1).getName();
            selectItem(position,titulo);
        }
    }

    private void selectItem(int position,String titulo) {
        //Remplazar los fragmentos

        listView.setItemChecked(position, true);
        getSupportActionBar().setTitle(titulo);

        tituloActivo=titulo;

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = null;
        ft= fm.beginTransaction().replace(R.id.container,FragmentAmigos.newInstance(titulo,titulo));

        ft.addToBackStack(null);
        if(false  || !BuildConfig.DEBUG)
            ft.commitAllowingStateLoss();
        else
            ft.commit();
        fm.executePendingTransactions();

        mDrawerLayout.closeDrawer(listView);

    }


    public void obtenerUsuario()
    {
        SharedPreferences sharedPref = getSharedPreferences("LoginPreferences",Context.MODE_PRIVATE);
        usuario = sharedPref.getString("usuario","null");
        contraseña = sharedPref.getString("pass","null");
        permisos = Integer.parseInt( sharedPref.getString("permisos","0") );
    }




}
