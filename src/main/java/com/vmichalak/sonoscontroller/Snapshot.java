package com.vmichalak.sonoscontroller;

import com.vmichalak.sonoscontroller.exception.SonosControllerException;
import com.vmichalak.sonoscontroller.model.PlayMode;
import com.vmichalak.sonoscontroller.model.PlayState;
import com.vmichalak.sonoscontroller.model.TrackInfo;
import com.vmichalak.sonoscontroller.model.TrackMetadata;

import java.io.IOException;

/* ========================= N O T E ========================
 * This class is an exact Java copy (including the comments)
 * of essential feature of the SoCo's Snapshot class,
 * which allows user to dump the current Sonos state
 * and restore it later.
 * The original source can be found here:
 * https://github.com/SoCo/SoCo/blob/master/soco/snapshot.py
 * ==========================================================
 */
public class Snapshot {
    private SonosDevice device;
    private String mediaUrl;
    private Boolean isCoordinator;
    private Boolean isPlayingQueue = false;
    private Boolean isPlayingCloudQueue = false;

    private Integer volume;
    private Boolean mute;
    private Integer bass;
    private Integer treble;
    private boolean loudness;

    private PlayMode playMode;
    //private Object crossFade;
    private Integer playlistPosition = 0;
    private String trackPosition;

    private TrackMetadata mediaMetadata;
    private PlayState transportState;

    public Snapshot(SonosDevice device) throws IOException, SonosControllerException {
        this.device = device;

        // get if device coordinator (or slave) True (or False)
        isCoordinator = device.isCoordinator();

        // Get information about the currently playing media
        TrackInfo currentTrackInfo = device.getCurrentTrackInfo();
        mediaUrl = currentTrackInfo.getUri();

        // Extract source from media uri - below some media URI value examples:
        //  'x-rincon-queue:RINCON_000E5859E49601400#0'
        //       - playing a local queue always #0 for local queue)
        //
        //  'x-rincon-queue:RINCON_000E5859E49601400#6'
        //       - playing a cloud queue where #x changes with each queue)
        //
        //  -'x-rincon:RINCON_000E5859E49601400'
        //       - a slave player pointing to coordinator player
        if ((mediaUrl.split(":"))[0].equals("x-rincon-queue")) {
            if ((mediaUrl.split("#"))[1].equals("0")) {
                // playing local queue
                isPlayingQueue = true;
            } else {
                // playing cloud queue - started from Alexa
                isPlayingCloudQueue = true;
            }
        }

        // Save the volume, mute and other sound settings
        volume = device.getVolume();
        mute = device.isMuted();
        bass = device.getBass();
        treble = device.getTreble();
        loudness = device.isLoudnessActivated();

        // get details required for what's playing
        if (isPlayingQueue) {
            // playing from queue - save repeat, random, cross fade, track, etc.
            playMode = device.getPlayMode();
            // X crossfade
            playlistPosition = currentTrackInfo.getQueueIndex();
            trackPosition = currentTrackInfo.getPosition();

        } else {
            mediaMetadata = currentTrackInfo.getMetadata();
        }

        // Work out what the playing state is - if a coordinator
        if (isCoordinator) {
            transportState = device.getPlayState();
        }
    }

    public void restore() throws IOException, SonosControllerException {
        // Start by ensuring that the speaker is paused as we don't want
        // things all rolling back when we are changing them, as this could
        // include things like audio
        PlayState playState = device.getPlayState();
        if (playState == PlayState.PLAYING) {
            device.pause();
        }

        // Reinstate what was playing
        if (isPlayingQueue && playlistPosition > 0) {
            // was playing from playlist

            // The position in the playlist returned by
            // get_current_track_info starts at 1, but when
            // playing from playlist, the index starts at 0
            // if position > 0:
            playlistPosition -= 1;
            device.playFromQueue(playlistPosition);

            if (trackPosition != null && trackPosition.length() > 0) {
                device.seek(trackPosition);
            }

            // reinstate track, position, play mode, cross fade
            // Need to make sure there is a proper track selected first
            device.setPlayMode(playMode);
            // X crossfade
        } else if (isPlayingCloudQueue) {
            // was playing a cloud queue started by Alexa
            // No way yet to re-start this so prevent it throwing an error!
        } else {
            // was playing a stream (radio station, file, or nothing)
            // reinstate uri and meta data
            if (mediaUrl.length() > 0) {
                device.setUri(mediaUrl, mediaMetadata.toDIDL());
            }
        }

        // For all devices:
        // Reinstate all the properties that are pretty easy to do
        device.setMute(mute);
        device.setBass(bass);
        device.setTreble(treble);
        device.setLoudness(loudness);

        // Reinstate volume
        // Can only change volume on device with fixed volume set to False
        // otherwise get uPnP error, so check first. Before issuing a network
        // command to check, fixed volume always has volume set to 100.
        // So only checked fixed volume if volume is 100.
        // TODO - fixedVolume is not supported so far
        boolean fixedVolume = false;

        //noinspection ConstantConditions
        if (!fixedVolume) {
            device.setVolume(volume);
        }

        // Now everything is set, see if we need to be playing, stopped
        // or paused ( only for coordinators)
        if (isCoordinator) {
            if (transportState == PlayState.PLAYING) {
                device.play();
            } else if (transportState == PlayState.STOPPED) {
                device.stop();
            }
        }
    }

    @Override
    public String toString() {
        return "Snapshot{" +
                "device=" + device +
                ", mediaUrl='" + mediaUrl + '\'' +
                ", isCoordinator=" + isCoordinator +
                ", isPlayingQueue=" + isPlayingQueue +
                ", isPlayingCloudQueue=" + isPlayingCloudQueue +
                ", volume=" + volume +
                ", mute=" + mute +
                ", bass=" + bass +
                ", treble=" + treble +
                ", loudness=" + loudness +
                ", playMode=" + playMode +
                ", playlistPosition=" + playlistPosition +
                ", trackPosition='" + trackPosition + '\'' +
                ", mediaMetadata=" + mediaMetadata +
                ", transportState=" + transportState +
                '}';
    }
}
