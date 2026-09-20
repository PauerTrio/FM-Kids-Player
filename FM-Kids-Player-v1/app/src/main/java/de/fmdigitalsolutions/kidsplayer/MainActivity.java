package de.fmdigitalsolutions.kidsplayer;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.*;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.media3.common.*;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import org.json.*;
import java.util.*;

public class MainActivity extends AppCompatActivity {
    private final ArrayList<SeriesItem> series = new ArrayList<>();
    private SeriesAdapter adapter;
    private ExoPlayer player;
    private PlayerView playerView;
    private View home;
    private int currentSeries = -1;
    private ArrayList<DocumentFile> currentVideos = new ArrayList<>();
    private String pendingName = "";
    private Uri pendingFolder;
    private final String PREF="fm_kids", KEY="series", PIN_KEY="pin";

    private final ActivityResultLauncher<Uri> folderPicker =
        registerForActivityResult(new ActivityResultContracts.OpenDocumentTree(), uri -> {
            if(uri==null) return;
            getContentResolver().takePersistableUriPermission(uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            pendingFolder=uri;
            askCover();
        });

    private final ActivityResultLauncher<String> coverPicker =
        registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if(uri!=null) {
                try { getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION); }
                catch(Exception ignored){}
            }
            addPending(uri);
        });

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        immersive();
        load();
        home=findViewById(R.id.home);
        playerView=findViewById(R.id.playerView);
        RecyclerView grid=findViewById(R.id.grid);
        grid.setLayoutManager(new GridLayoutManager(this, 2));
        adapter=new SeriesAdapter(series, this::playSeries);
        grid.setAdapter(adapter);

        TextView title=findViewById(R.id.title);
        title.setOnLongClickListener(v -> { pinDialog(); return true; });

        player=new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        playerView.setUseController(true);
        player.addListener(new Player.Listener() {
            @Override public void onPlaybackStateChanged(int state) {
                if(state==Player.STATE_ENDED) advance();
            }
        });

        if(series.isEmpty()) firstStart();
    }

    private void immersive() {
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    }

    private void firstStart() {
        new AlertDialog.Builder(this).setTitle("FM Kids Player")
            .setMessage("Noch keine Serien eingerichtet.\n\nHalte später die Überschrift lange gedrückt, um den Elternbereich zu öffnen.\n\nStandard-PIN: 2580")
            .setPositiveButton("Elternbereich öffnen",(d,w)->adminMenu()).show();
    }

    private void pinDialog() {
        final EditText e=new EditText(this); e.setInputType(2); e.setHint("PIN");
        new AlertDialog.Builder(this).setTitle("Elternbereich").setView(e)
            .setPositiveButton("Öffnen",(d,w)->{
                String pin=getPreferences().getString(PIN_KEY,"2580");
                if(e.getText().toString().equals(pin)) adminMenu();
                else Toast.makeText(this,"Falsche PIN",Toast.LENGTH_SHORT).show();
            }).setNegativeButton("Abbrechen",null).show();
    }

    private void adminMenu() {
        String[] items={"Serie hinzufügen","Serien verwalten","Fortschritt zurücksetzen","PIN ändern","Kiosk / Startbildschirm-Hilfe"};
        new AlertDialog.Builder(this).setTitle("Elternbereich").setItems(items,(d,which)->{
            if(which==0) askName();
            if(which==1) manage();
            if(which==2) resetProgress();
            if(which==3) changePin();
            if(which==4) kioskHelp();
        }).setNegativeButton("Schließen",null).show();
    }

    private void askName() {
        EditText e=new EditText(this); e.setHint("z. B. Mickey Maus");
        new AlertDialog.Builder(this).setTitle("Name der Serie").setView(e)
            .setPositiveButton("Weiter",(d,w)->{
                pendingName=e.getText().toString().trim();
                if(!pendingName.isEmpty()) folderPicker.launch(null);
            }).setNegativeButton("Abbrechen",null).show();
    }

    private void askCover() {
        new AlertDialog.Builder(this).setTitle("Titelbild")
            .setMessage("Möchtest du ein Bild für die Serien-Kachel auswählen?")
            .setPositiveButton("Bild auswählen",(d,w)->coverPicker.launch("image/*"))
            .setNegativeButton("Ohne Bild",(d,w)->addPending(null)).show();
    }

    private void addPending(Uri cover) {
        if(pendingFolder==null) return;
        series.add(new SeriesItem(UUID.randomUUID().toString(),pendingName,pendingFolder.toString(),
                cover==null ? "" : cover.toString(),0));
        save(); adapter.notifyDataSetChanged();
        Toast.makeText(this,"Serie hinzugefügt",Toast.LENGTH_SHORT).show();
    }

    private void manage() {
        if(series.isEmpty()){ Toast.makeText(this,"Keine Serien vorhanden",Toast.LENGTH_SHORT).show(); return; }
        String[] names=new String[series.size()];
        for(int i=0;i<series.size();i++) names[i]=series.get(i).name;
        new AlertDialog.Builder(this).setTitle("Serien verwalten").setItems(names,(d,i)->editSeries(i)).show();
    }

    private void editSeries(int i) {
        String[] opts={"Nach oben","Nach unten","Fortschritt dieser Serie zurücksetzen","Serie entfernen"};
        new AlertDialog.Builder(this).setTitle(series.get(i).name).setItems(opts,(d,w)->{
            if(w==0 && i>0) Collections.swap(series,i,i-1);
            if(w==1 && i<series.size()-1) Collections.swap(series,i,i+1);
            if(w==2) series.get(i).progress=0;
            if(w==3) series.remove(i);
            save(); adapter.notifyDataSetChanged();
        }).show();
    }

    private ArrayList<DocumentFile> videosFor(SeriesItem s) {
        ArrayList<DocumentFile> out=new ArrayList<>();
        DocumentFile dir=DocumentFile.fromTreeUri(this,Uri.parse(s.folderUri));
        if(dir!=null) for(DocumentFile f:dir.listFiles()) {
            String type=f.getType();
            if(f.isFile() && ((type!=null && type.startsWith("video/")) || isVideoName(f.getName()))) out.add(f);
        }
        out.sort((a,b)->natural(a.getName(),b.getName()));
        return out;
    }

    private boolean isVideoName(String n) {
        if(n==null) return false; n=n.toLowerCase(Locale.ROOT);
        return n.endsWith(".mp4")||n.endsWith(".mkv")||n.endsWith(".webm")||n.endsWith(".m4v");
    }

    private int natural(String a,String b) {
        if(a==null)a=""; if(b==null)b="";
        return a.compareToIgnoreCase(b); // Empfehlung: 01, 02, 03 ... benennen
    }

    private void playSeries(int index) {
        if(index<0||index>=series.size()) return;
        currentSeries=index;
        currentVideos=videosFor(series.get(index));
        if(currentVideos.isEmpty()) {
            Toast.makeText(this,"Keine Videos in diesem Ordner gefunden",Toast.LENGTH_LONG).show(); return;
        }
        if(series.get(index).progress>=currentVideos.size()) series.get(index).progress=0;
        playCurrent();
    }

    private void playCurrent() {
        SeriesItem s=series.get(currentSeries);
        currentVideos=videosFor(s);
        if(s.progress>=currentVideos.size()){ advanceSeries(); return; }
        home.setVisibility(View.GONE); playerView.setVisibility(View.VISIBLE);
        player.setMediaItem(MediaItem.fromUri(currentVideos.get(s.progress).getUri()));
        player.prepare(); player.play();
        immersive();
    }

    private void advance() {
        if(currentSeries<0) return;
        SeriesItem s=series.get(currentSeries);
        s.progress++;
        save();
        currentVideos=videosFor(s);
        if(s.progress < currentVideos.size()) playCurrent();
        else advanceSeries();
    }

    private void advanceSeries() {
        if(series.isEmpty()){ showHome(); return; }
        // Nächste Serie mit noch nicht gesehenen Folgen suchen.
        for(int step=1;step<=series.size();step++) {
            int idx=(currentSeries+step)%series.size();
            int count=videosFor(series.get(idx)).size();
            if(count>0 && series.get(idx).progress<count) { currentSeries=idx; playCurrent(); return; }
        }
        // Alles gesehen: kompletter Zyklus beginnt wieder von vorne.
        for(SeriesItem s:series) s.progress=0;
        save();
        int next=series.size()>1 ? (currentSeries+1)%series.size() : 0;
        currentSeries=next;
        playCurrent();
    }

    private void showHome() {
        player.stop(); playerView.setVisibility(View.GONE); home.setVisibility(View.VISIBLE); immersive();
    }

    @Override public void onBackPressed() {
        if(playerView.getVisibility()==View.VISIBLE) {
            // Kind kommt mit Zurück nur zur Serienauswahl, nicht aus der App.
            showHome();
        } else {
            // Absichtlich nichts: Kids-Oberfläche bleibt geöffnet.
        }
    }

    private void resetProgress() {
        for(SeriesItem s:series)s.progress=0; save();
        Toast.makeText(this,"Fortschritt zurückgesetzt",Toast.LENGTH_SHORT).show();
    }

    private void changePin() {
        EditText e=new EditText(this); e.setInputType(2); e.setHint("Neue PIN");
        new AlertDialog.Builder(this).setTitle("PIN ändern").setView(e)
            .setPositiveButton("Speichern",(d,w)->{
                String p=e.getText().toString();
                if(p.length()>=4){ getPreferences().edit().putString(PIN_KEY,p).apply(); Toast.makeText(this,"PIN geändert",Toast.LENGTH_SHORT).show(); }
                else Toast.makeText(this,"Mindestens 4 Ziffern",Toast.LENGTH_SHORT).show();
            }).setNegativeButton("Abbrechen",null).show();
    }

    private void kioskHelp() {
        new AlertDialog.Builder(this).setTitle("S20 FE – Sperrmodus")
            .setMessage("Die App kann als Standard-Startbildschirm (Launcher) gewählt werden. Dann startet nach dem Einschalten direkt FM Kids Player und die Home-Taste führt zurück zur App.\n\nFür zusätzliche Sperre kannst du in Samsung/Android außerdem „App anheften“ aktivieren.\n\nWichtig: Eine absolut manipulationssichere Device-Owner-Sperre benötigt eine einmalige Geräte-Provisionierung und lässt sich nicht allein durch Installation einer APK erzwingen.")
            .setPositiveButton("OK",null).show();
    }

    private android.content.SharedPreferences getPreferences() {
        return getSharedPreferences(PREF,MODE_PRIVATE);
    }

    private void load() {
        String raw=getPreferences().getString(KEY,"[]");
        try {
            JSONArray a=new JSONArray(raw);
            for(int i=0;i<a.length();i++) series.add(SeriesItem.fromJson(a.getJSONObject(i)));
        } catch(Exception ignored){}
    }

    private void save() {
        JSONArray a=new JSONArray();
        for(SeriesItem s:series)a.put(s.toJson());
        getPreferences().edit().putString(KEY,a.toString()).apply();
    }

    @Override protected void onDestroy() {
        if(player!=null)player.release();
        super.onDestroy();
    }
}
