package com.tdt.neumaticos.Helper;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.tdt.neumaticos.BuildConfig;
import com.tdt.neumaticos.Fragments.FragmentAmigos;
import com.tdt.neumaticos.Fragments.FragmentCerrar;
import com.tdt.neumaticos.Fragments.FragmentContent;
import com.tdt.neumaticos.Fragments.FragmentCategorias;
import com.tdt.neumaticos.Interface.NavigationManager;
import com.tdt.neumaticos.MainActivity;
import com.tdt.neumaticos.R;

public class FragmentNavigationManager implements NavigationManager {

    private static FragmentNavigationManager mInstance;
    private FragmentManager mFragmentManager;
    private MainActivity mainActivity;

    public static FragmentNavigationManager getmInstance(MainActivity mainActivity)
    {
        if(mInstance== null)
        {
            mInstance= new FragmentNavigationManager();
        }
        mInstance.configure(mainActivity);
        return mInstance;
    }

    private void configure(MainActivity mainActivity) {
        mainActivity=mainActivity;
        mFragmentManager = mainActivity.getSupportFragmentManager();
    }

    @Override
    public void showFragment(String title) {

        FragmentManager fm = mFragmentManager;
        FragmentTransaction ft = null;
        
        switch (title)
        {
            case "Categorías|Principal":
            case "Categorías|Acción":
            case "Categorías|Documentales":
            case "Categorías|Fantasía":
            case "Categorías|Infantiles":
            case "Categorías|Romanticas":
            case "Categorías|Terror":
            case "Categorías|Otros":
                ft= fm.beginTransaction().replace(R.id.container,FragmentCategorias.newInstance(title));
                break;
            case "Amigos|Buscar":
                ft= fm.beginTransaction().replace(R.id.container,FragmentAmigos.newInstance(title,"buscar"));
                break;
            case "Amigos|Mis amigos":
                ft= fm.beginTransaction().replace(R.id.container,FragmentAmigos.newInstance(title,"misamigos"));
                break;
            case "Perfil|Cerrar sesión":
                ft= fm.beginTransaction().replace(R.id.container,FragmentCerrar.newInstance(title));
                break;
            case "Perfil|Historial":
                ft= fm.beginTransaction().replace(R.id.container,FragmentCerrar.newInstance(title));
                break;

             default:
                 ft= fm.beginTransaction().replace(R.id.container,FragmentContent.newInstance(title));
                 break;
        }

        ft.addToBackStack(null);
        if(false  || !BuildConfig.DEBUG)
            ft.commitAllowingStateLoss();
        else
            ft.commit();
        fm.executePendingTransactions();

    }


    /*
    @Override
    public void showFragment(String title) {

        showFragment(FragmentContent.newInstance(title),false);

        if(title.equals("Categorías|Principal"))
        {
            //showFragmentCat(FragmentCategorias.newInstance(title),false);

            FragmentManager fm = mFragmentManager;
            FragmentTransaction ft= fm.beginTransaction().replace(R.id.container,FragmentCategorias.newInstance(title));
            ft.addToBackStack(null);
            if(false  || !BuildConfig.DEBUG)
                ft.commitAllowingStateLoss();
            else
                ft.commit();
            fm.executePendingTransactions();


            Log.d("salidas","ENTRO AQUI");
        }
        Log.d("salidas","ENTRO AQUI TAMBIEN");

    }

    private void showFragment(FragmentContent fragmentContent, boolean b) {
        FragmentManager fm = mFragmentManager;
        FragmentTransaction ft= fm.beginTransaction().replace(R.id.container,fragmentContent);
        ft.addToBackStack(null);
        if(b  || !BuildConfig.DEBUG)
            ft.commitAllowingStateLoss();
        else
            ft.commit();
        fm.executePendingTransactions();
    }
    */


}
