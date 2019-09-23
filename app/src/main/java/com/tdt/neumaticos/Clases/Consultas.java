package com.tdt.neumaticos.Clases;

public class Consultas {
    public String categoria(String cat)
    {
        switch (cat)
        {
            case "Categorías|Principal":
                cat="*";
                break;
            case "Categorías|Acción":
                cat="Accion";
                break;
            case "Categorías|Documentales":
                cat="Documental";
                break;
            case "Categorías|Fantasía":
                cat="Fantasia";
                break;
            case "Categorías|Infantiles":
                cat="Animacion";
                break;
            case "Categorías|Romanticas":
                cat="Romantica";
                break;
            case "Categorías|Terror":
                cat="Terror";
                break;
            case "Categorías|Otros":
                cat="%";
                break;

            default:
                cat="%";
                break;
        }
        return cat;
    }
}
