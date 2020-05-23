package com.vmichalak.sonoscontroller.model;

import com.vmichalak.sonoscontroller.ParserHelper;

public class TrackMetadata {
    private final String protocolInfo;
    private final String res;
    private final String streamContent;
    private final String title;
    private final String creator;
    private final String albumArtist;
    private final String album;
    private final String albumArtURI;

    public TrackMetadata(String protocolInfo, String res, String streamContent, String title, String creator, String albumArtist, String album, String albumArtURI) {
        this.protocolInfo = protocolInfo;
        this.res = res;
        this.streamContent = streamContent;
        this.title = title;
        this.creator = creator;
        this.albumArtist = albumArtist;
        this.album = album;
        this.albumArtURI = albumArtURI;
    }

    public static TrackMetadata parse(String metadata) {
        System.out.println(metadata);
        return new TrackMetadata(
                ParserHelper.findOne("<res protocolInfo=\"(.*)\">", metadata),
                ParserHelper.findOne("<res protocolInfo=\".*\">(.*)</res>", metadata),
                ParserHelper.findOne("<r:streamContent>>(.*)</r:streamContent>>", metadata),
                ParserHelper.findOne("<dc:title>(.*)</dc:title>", metadata),
                ParserHelper.findOne("<dc:creator>(.*)</dc:creator>", metadata),
                ParserHelper.findOne("<r:albumArtist>(.*)</r:albumArtist>", metadata),
                ParserHelper.findOne("<upnp:album>(.*)</upnp:album>", metadata),
                ParserHelper.findOne("<upnp:albumArtURI>(.*)</upnp:albumArtURI>", metadata)
        );
    }

    public String getProtocolInfo() {
        return protocolInfo;
    }

    public String getRes() {
        return res;
    }

    public String getStreamContent() {
        return streamContent;
    }

    public String getTitle() {
        return title;
    }

    public String getCreator() {
        return creator;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public String getAlbum() {
        return album;
    }

    public String getAlbumArtURI() {
        return albumArtURI;
    }

    public String toDIDL() {
        return "<DIDL-Lite xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:upnp=\"urn:schemas-upnp-org:metadata-1-0/upnp/\" xmlns:r=\"urn:schemas-rinconnetworks-com:metadata-1-0/\" xmlns=\"urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/\">" +
                "<item id=\"-1\" parentID=\"-1\" restricted=\"true\">" +
                "<res protocolInfo=\"" + protocolInfo + "\">" + res + "</res>" +
                "<r:streamContent>" + streamContent + "</r:streamContent>" +
                "<dc:title>" + title + "</dc:title>" +
                "<dc:creator>" + creator + "</dc:creator>" +
                "<r:albumArtist>" + albumArtist + "</r:albumArtist>" +
                "<upnp:album>" + album + "</upnp:album>" +
                "<upnp:albumArtURI>" + albumArtURI + "</upnp:albumArtURI>" +
                "</item>" +
                "</DIDL-Lite>";
    }

    @Override
    public String toString() {
        return "TrackMetadata{" +
                "title='" + title + '\'' +
                ", creator='" + creator + '\'' +
                ", albumArtist='" + albumArtist + '\'' +
                ", album='" + album + '\'' +
                ", albumArtURI='" + albumArtURI + '\'' +
                '}';
    }
}
