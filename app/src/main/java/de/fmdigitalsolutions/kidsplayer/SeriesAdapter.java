package de.fmdigitalsolutions.kidsplayer;

import android.net.Uri;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SeriesAdapter extends RecyclerView.Adapter<SeriesAdapter.H> {
    public interface Click { void onClick(int position); }
    private final List<SeriesItem> data;
    private final Click click;

    public SeriesAdapter(List<SeriesItem> data, Click click) { this.data=data; this.click=click; }

    @NonNull public H onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new H(LayoutInflater.from(p.getContext()).inflate(R.layout.item_series,p,false));
    }
    public void onBindViewHolder(@NonNull H h, int pos) {
        SeriesItem s=data.get(pos);
        h.name.setText(s.name);
        h.fallback.setVisibility(View.VISIBLE);
        h.cover.setImageDrawable(null);
        if(s.coverUri!=null && !s.coverUri.isEmpty()) {
            try {
                h.cover.setImageURI(Uri.parse(s.coverUri));
                h.fallback.setVisibility(View.GONE);
            } catch(Exception ignored) {}
        }
        h.itemView.setOnClickListener(v -> click.onClick(h.getBindingAdapterPosition()));
    }
    public int getItemCount(){ return data.size(); }
    static class H extends RecyclerView.ViewHolder {
        ImageView cover; TextView name,fallback;
        H(View v){ super(v); cover=v.findViewById(R.id.cover); name=v.findViewById(R.id.name); fallback=v.findViewById(R.id.fallback); }
    }
}
