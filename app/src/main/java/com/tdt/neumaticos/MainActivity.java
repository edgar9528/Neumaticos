package com.tdt.neumaticos;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
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
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;

import com.tdt.neumaticos.Adapter.CustomExpandableListAdapter;
import com.tdt.neumaticos.Adapter.DrawerListAdapter;
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

    ArrayList<DrawerItem> elemenosMenu;

    private ListView listView;

    String user_id="",user_nombre="",user_correo="",user_contrasena="";

    @Override
    public void onBackPressed() {

        AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);
        dialogo1.setTitle("Importante");
        dialogo1.setMessage("¿Desea salir de la app?");
        dialogo1.setCancelable(false);
        dialogo1.setPositiveButton("Si", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogo1, int id) {

                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                /*finish();
                System.exit(0);*/
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

        initItems();

        View listHeaderView = getLayoutInflater().inflate(R.layout.nav_header,null,false);
        listView.addHeaderView(listHeaderView);

        TextView textView = findViewById(R.id.textViewNombre);
        textView.setText(user_nombre);

        genData();

        addDrawersItem();
        setupDrawer();

        if(savedInstanceState== null)
        {
            selectFirsItemAsDefault();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Categorías|Principal");


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

        elemenosMenu.add(new DrawerItem("e1", R.drawable.logotdt));
        elemenosMenu.add(new DrawerItem("e2", R.drawable.logotdt));

        //adapter = new CustomExpandableListAdapter(this,lstTitle,lstChild);
        listView.setAdapter(new DrawerListAdapter(this, elemenosMenu));
        //listView.setAdapter(adapter);


        /*

        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                //set title for toolbar
                //getSupportActionBar().setTitle(lstTitle.get(groupPosition).toString());
            }
        });

        expandableListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {
            @Override
            public void onGroupCollapse(int groupPosition) {
                //getSupportActionBar().setTitle("Inicio");
            }
        });


        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

                String selectedItem = ( (List) (lstChild.get(lstTitle.get(groupPosition))) )
                        .get(childPosition).toString();


                String clave= lstTitle.get(groupPosition)  +"|"+selectedItem;
                Log.d("Salida", clave);


                getSupportActionBar().setTitle(clave);


                navigationManager.showFragment(clave);

                mDrawerLayout.closeDrawer(GravityCompat.START);

                return false;
            }
        });


        */


    }

    private void setupDrawer() {
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.string.open,R.string.close)
        {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                //getSupportActionBar().setTitle("Inicio2");
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                //getSupportActionBar().setTitle(mActivityTitle);
                invalidateOptionsMenu();
            }
        };


        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

    }

    private void genData() {


    }

    private void initItems() {
        items = new String[]{"Android Programing","Xamarin Programing","iOS Programing"};
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(mDrawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);
    }

    public void obtenerUsuario()
    {
        SharedPreferences sharedPref = getSharedPreferences("LoginPreferences",Context.MODE_PRIVATE);
        user_id = sharedPref.getString("user_id","null");
        user_nombre = sharedPref.getString("user_nombre","null");
        user_correo = sharedPref.getString("user_correo","null");
        user_contrasena = sharedPref.getString("user_contrasena","null");
    }

}
