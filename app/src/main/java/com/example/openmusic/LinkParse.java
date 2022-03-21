package com.example.openmusic;

public class LinkParse {

    public static String parse(String link){
        if(link.contains("music.youtube")){
            return parseYoutubeMusicLink(link);
        }
        if(link.contains("music.yandex")){
            return parseYandexLink(link);
        }
        if(link.contains("youtu.be")){
            return parseYoutubeLink(link);
        }
        return "";
    }

    public static String parseYandexLink(String link){
        String[] isbnParts = link.split("/");
        return isbnParts[isbnParts.length-1];
    }

    public static String parseYoutubeLink(String link){
        String[] isbnParts = link.split("/");
        return isbnParts[isbnParts.length-1];
    }

    public static String parseYoutubeMusicLink(String link){
        int start = link.indexOf("=");
        int end = link.indexOf("&");
        return link.substring(start+1, end);
    }
}
