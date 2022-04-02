package com.example.openmusic;

public class LinkParse {

    public enum LinkType{
        NONE,
        YANDEX_TRACK,
        YANDEX_ALBUM,
        YOUTUBE,
        YOUTUBE_MUSIC
    }

    private String id;
    private LinkType linkType;


    public String getId() {
        return id;
    }

    public LinkType getLinkType() {
        return linkType;
    }




    public void parse(String link){
        this.linkType = LinkType.NONE;
        if(link.contains("music.youtube")){
            this.id = parseYoutubeMusicLink(link);
            this.linkType = LinkType.YOUTUBE_MUSIC;
        }
        if(link.contains("youtu.be")){
            this.id = parseYoutubeLink(link);
            this.linkType = LinkType.YOUTUBE;
        }
        if(link.contains("music.yandex")){
            this.id = parseYandexLink(link);
            this.linkType = LinkType.YANDEX_TRACK;
        }
        if(link.contains("music.yandex") && !link.contains("track") && link.contains("album")){
            this.id = parseYandexLink(link);
            this.linkType = LinkType.YANDEX_ALBUM;
        }
        if(link.contains("music.yandex") && link.contains("track")){
            this.id = parseYandexLink(link);
            this.linkType = LinkType.YANDEX_TRACK;
        }


    }

    public String parseYandexLink(String link){
        String[] isbnParts = link.split("/");
        return isbnParts[isbnParts.length-1];
    }

    public String parseYoutubeLink(String link){
        String[] isbnParts = link.split("/");
        return isbnParts[isbnParts.length-1];
    }

    public String parseYoutubeMusicLink(String link){
        int start = link.indexOf("=");
        int end = link.indexOf("&");
        return link.substring(start+1, end);
    }
}
