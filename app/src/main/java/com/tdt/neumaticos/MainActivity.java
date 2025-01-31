package com.tdt.neumaticos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tdt.neumaticos.Adapter.DrawerListAdapter;
import com.tdt.neumaticos.Fragments.LeercodigoFragment;
import com.tdt.neumaticos.Fragments.SeleccionarutaFragment;
import com.tdt.neumaticos.Fragments.VacioFragment;
import com.tdt.neumaticos.Model.DrawerItem;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    ArrayList<DrawerItem> elementosMenu;
    String tituloActivo="";

    private ListView listView;
    private String dato;

    String usuario,contraseña;
    int permisos;

    @Override
    public void onBackPressed() {
        cerrarSesion();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {

            obtenerUsuario();

            //init view
            mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            listView = findViewById(R.id.navList);

            View listHeaderView = getLayoutInflater().inflate(R.layout.nav_header, null, false);
            listView.addHeaderView(listHeaderView);

            TextView textView = findViewById(R.id.textViewNombre);
            textView.setText(usuario);

            addDrawersItem();
            setupDrawer();

            if (savedInstanceState == null) {
                selectItem(1, elementosMenu.get(0).getName());
            }
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "Eror: "+e.toString(), Toast.LENGTH_SHORT).show();
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

    private void addDrawersItem() {

        elementosMenu = new ArrayList<DrawerItem>();
        String nombresMenu[] ={"Alta","Montaje","Configuración","Mantenimiento",
                                "Cambia ubicación","Baja","Salida","Entrada","Cerrar sesión"};
        int iconos[] = {R.drawable.ic_file_upload,R.drawable.ic_montaje,R.drawable.ic_config,R.drawable.ic_action_shield,
                        R.drawable.ic_cambia,R.drawable.ic_file_download,R.drawable.ic_salida,R.drawable.ic_entrada,
                        R.drawable.ic_exit_to_app};


        for(int i=0,b=1;i<8;i++,b=b+b)
        {
            if( (permisos&b)==b)
            {
                elementosMenu.add(new DrawerItem(nombresMenu[i], iconos[i]));
            }
        }

        elementosMenu.add(new DrawerItem(nombresMenu[nombresMenu.length-1], iconos[iconos.length-1]));

        listView.setAdapter(new DrawerListAdapter(this, elementosMenu));

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

            //cuando en el menu no se toca el espacio de perfil y fotografia
            if(position>0)
            {
                String titulo = elementosMenu.get(position - 1).getName();

                if (!titulo.equals("Configuración") && !titulo.equals("Cerrar sesión")) {
                    //si no es la ventana de configuración
                    selectItem(position, titulo);
                    //Log.d("salida",titulo);
                } else {
                    if (titulo.equals("Configuración")) {
                        Intent intent = new Intent(MainActivity.this, ConfiguracionActivity.class);
                        startActivity(intent);
                        mDrawerLayout.closeDrawer(listView);
                    } else {
                        cerrarSesion();
                    }
                }
            }
        }
    }

    private void cerrarSesion()
    {
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
                mDrawerLayout.closeDrawer(listView);
            }
        });
        dialogo1.show();
    }

    private void selectItem(int position,String titulo) {
        //Remplazar los fragmentos

        try {

            listView.setItemChecked(position, true);
            getSupportActionBar().setTitle(titulo);

            tituloActivo = titulo;

            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = null;

            Fragment fragment;

            switch (titulo) {
                case "Alta":
                    dato="Alta";
                    fragment = new LeercodigoFragment();
                    break;
                case "Montaje":
                    dato="Montaje";
                    fragment = new SeleccionarutaFragment();
                    break;
                case "Mantenimiento":
                    dato="Mantenimiento";
                    fragment = new LeercodigoFragment();
                    break;
                case "Cambia ubicación":
                    dato="Cambia";
                    fragment = new LeercodigoFragment();
                    break;
                case "Baja":
                    dato="Baja";
                    fragment = new LeercodigoFragment();
                    break;
                case "Salida":
                    dato="Salida";
                    fragment = new SeleccionarutaFragment();
                    break;
                case "Entrada":
                    dato="Entrada";
                    fragment = new SeleccionarutaFragment();
                    break;
                default:
                    dato="Alta";
                    fragment = new VacioFragment();
                    break;
            }

            ft = fm.beginTransaction().replace(R.id.container, fragment);

            ft.addToBackStack(null);
            if (false || !BuildConfig.DEBUG)
                ft.commitAllowingStateLoss();
            else
                ft.commit();
            fm.executePendingTransactions();

            mDrawerLayout.closeDrawer(listView);
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "Error: "+e.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    public String getDataFragmento()
    {
        return dato;
    }

    public String getUsuarioActivity()
    {
        return usuario;
    }

    public void obtenerUsuario()
    {
        SharedPreferences sharedPref = getSharedPreferences("LoginPreferences",Context.MODE_PRIVATE);
        usuario = sharedPref.getString("usuario","null");
        contraseña = sharedPref.getString("pass","null");
        permisos = Integer.parseInt( sharedPref.getString("permisos","0") );
    }

}
