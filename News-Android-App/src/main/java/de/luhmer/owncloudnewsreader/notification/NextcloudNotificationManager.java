package de.luhmer.owncloudnewsreader.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.io.File;

import de.luhmer.owncloudnewsreader.BuildConfig;
import de.luhmer.owncloudnewsreader.NewsReaderListActivity;
import de.luhmer.owncloudnewsreader.R;

public class NextcloudNotificationManager {

    private static final int ID_DownloadSingleImageComplete = 10;
    private static final int UNREAD_RSS_ITEMS_NOTIFICATION_ID = 246;

    public static void showNotificationDownloadSingleImageComplete(Context context, File imagePath) {
        String channelDownloadImage = context.getString(R.string.action_img_download);
        NotificationManager notificationManager = getNotificationManagerAndCreateChannel(context, channelDownloadImage);

        // Load image, decode it to Bitmap and return Bitmap to callback
        ImageSize targetSize = new ImageSize(1024,512); // result Bitmap will be fit to this size
        Bitmap bitmap = ImageLoader.getInstance().loadImageSync("file://" + imagePath.getAbsolutePath(), targetSize);

        // Uri imageUri = Uri.parse(imagePath);
        Uri imageUri = FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".provider",
                imagePath);

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(imageUri, "image/*");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);


        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelDownloadImage)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(context.getString(R.string.toast_img_saved))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap));

        notificationManager.notify(ID_DownloadSingleImageComplete, mBuilder.build());
    }



    public static NotificationCompat.Builder buildNotificationDownloadImageService(Context context, String channelId) {
        getNotificationManagerAndCreateChannel(context, channelId);

        Intent intentNewsReader = new Intent(context, NewsReaderListActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intentNewsReader, 0);
        NotificationCompat.Builder mNotificationDownloadImages = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText("Downloading images for offline usage")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setOngoing(true);

        return mNotificationDownloadImages;
    }

    public static void showNotificationSaveSingleCachedImageService(Context context, String channelId, File file) {
        NotificationManager notificationManager = getNotificationManagerAndCreateChannel(context, channelId);

        //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(file));
        //PendingIntent contentIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder mNotificationDownloadImages = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText("Saved image to: " + file.getPath())
                .setSmallIcon(R.drawable.ic_notification);
                //.setContentIntent(contentIntent);


        notificationManager.notify(1235, mNotificationDownloadImages.build());
    }

    public static NotificationCompat.Builder buildNotificationDownloadWebPageService(Context context, String channelId) {
        getNotificationManagerAndCreateChannel(context, channelId);

        Intent intentNewsReader = new Intent(context, NewsReaderListActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intentNewsReader, 0);
        NotificationCompat.Builder mNotificationWebPages = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText("Downloading webpages for offline usage")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setOngoing(true);

        return mNotificationWebPages;
    }



    public static void showNotificationImageDownloadLimitReached(Context context, String channelId, int limit) {
        NotificationManager notificationManager = getNotificationManagerAndCreateChannel(context, channelId);

        Intent intentNewsReader = new Intent(context, NewsReaderListActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intentNewsReader, 0);
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(context, channelId)
                .setContentTitle("Nextcloud News")
                .setContentText("Only " + limit + " images can be cached at once")
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pIntent);

        Notification notify = notifyBuilder.build();

        //Hide the notification after its selected
        notify.flags |= Notification.FLAG_AUTO_CANCEL;

        // Use random ID
        notificationManager.notify(123, notify);
    }

    /**
     * Build a notification using the information from the given media session. Makes heavy use
     * of {@link MediaMetadataCompat#getDescription()} to extract the appropriate information.
     * @param context Context used to construct the notification.
     * @param mediaSession Media session to get information.
     * @return A pre-built notification with information from the given media session.
     */
    public static NotificationCompat.Builder buildPodcastNotification(Context context, String channelId, MediaSessionCompat mediaSession) {
        getNotificationManagerAndCreateChannel(context, channelId);

        /*
        // Creates an explicit intent for an ResultActivity to receive.
        Intent resultIntent = new Intent(context, NewsReaderListActivity.class);
        // Because clicking the notification opens a new ("special") activity, there's
        // no need to create an artificial back stack.
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(resultPendingIntent);
        */

        Bitmap bitmapIcon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);

        MediaControllerCompat controller = mediaSession.getController();
        MediaMetadataCompat mediaMetadata = controller.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                /*
                .setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(
                                new int[]{playPauseButtonPosition})  // show only play/pause in compact view
                        .setMediaSession(mSession.getSessionToken()))
                */
                //.setUsesChronometer(true)
                .setContentTitle(description.getTitle())
                .setSmallIcon(R.drawable.ic_notification)
                //.setContentText(description.getSubtitle())
                //.setContentText(mediaMetadata.getText(MediaMetadataCompat.METADATA_KEY_ARTIST))
                //.setSubText(description.getDescription())
                //.setLargeIcon(description.getIconBitmap())
                .setLargeIcon(bitmapIcon)
                .setContentIntent(controller.getSessionActivity())
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(context, PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true);

        boolean isPlaying = controller.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING;
        builder.addAction(getPlayPauseAction(context, isPlaying));

        builder.setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
            //.setShowActionsInCompactView(0)  // show only play/pause in compact view
            .setMediaSession(mediaSession.getSessionToken())
            .setShowCancelButton(true)
            .setCancelButtonIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context, PlaybackStateCompat.ACTION_STOP)));


        return builder;
    }

    private static NotificationCompat.Action getPlayPauseAction(Context context, boolean isPlaying) {
        int drawableId = isPlaying ? android.R.drawable.ic_media_pause : android.R.drawable.ic_media_play;
        String actionText = isPlaying ? "Pause" : "Play"; // TODO extract as string resource

        PendingIntent pendingIntent = MediaButtonReceiver.buildMediaButtonPendingIntent(context,
                isPlaying ? PlaybackStateCompat.ACTION_PAUSE : PlaybackStateCompat.ACTION_PLAY);
        return new NotificationCompat.Action(drawableId, actionText,  pendingIntent);

    }

    public static NotificationCompat.Builder buildDownloadPodcastNotification(Context context, String channelId) {
        getNotificationManagerAndCreateChannel(context, channelId);

        Intent intentNewsReader = new Intent(context, NewsReaderListActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(context, 0, intentNewsReader, 0);
        NotificationCompat.Builder mNotificationDownloadPodcast = new NotificationCompat.Builder(context, channelId)
                .setContentTitle(context.getResources().getString(R.string.app_name))
                .setContentText(context.getString(R.string.notification_downloading_podcast_title))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setOngoing(true);

        return mNotificationDownloadPodcast;
    }




    public static void showUnreadRssItemsNotification(Context context, int newItemsCount) {
        Resources res = context.getResources();
        String tickerMessage = res.getQuantityString(R.plurals.notification_new_items_ticker, newItemsCount, newItemsCount);
        String contentText = res.getQuantityString(R.plurals.notification_new_items_text, newItemsCount, newItemsCount);
        String title = context.getString(R.string.app_name);

        String channelId = context.getString(R.string.app_name);
        NotificationManager notificationManager = getNotificationManagerAndCreateChannel(context, channelId);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, channelId)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setTicker(tickerMessage)
                        .setContentTitle(title)
                        //.setDefaults(Notification.DEFAULT_ALL)
                        .setAutoCancel(true)
                        .setContentText(contentText);


        Intent notificationIntent = new Intent(context, NewsReaderListActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, UNREAD_RSS_ITEMS_NOTIFICATION_ID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(contentIntent);

        notificationManager.notify(UNREAD_RSS_ITEMS_NOTIFICATION_ID, builder.build());
    }

    public static boolean isUnreadRssCountNotificationVisible(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for(StatusBarNotification statusBarNotification : notificationManager.getActiveNotifications()) {
                if(statusBarNotification.getId() == UNREAD_RSS_ITEMS_NOTIFICATION_ID) {
                    return true;
                }
            }
        }
        return false;

    }

    public static void removeRssItemsNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.cancel(UNREAD_RSS_ITEMS_NOTIFICATION_ID);
    }














    private static NotificationManager getNotificationManagerAndCreateChannel(Context context, String channelId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel mChannel = new NotificationChannel(channelId, channelId, importance);
            mChannel.setSound(null, null);
            mChannel.enableVibration(false);
            //mChannel.setShowBadge(false);
            //mChannel.enableLights(true);
            notificationManager.createNotificationChannel(mChannel);
        }
        return notificationManager;
    }

}
