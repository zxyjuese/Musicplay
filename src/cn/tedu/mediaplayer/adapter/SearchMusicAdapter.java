package cn.tedu.mediaplayer.adapter;

import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import cn.tedu.mediaplayer.R;
import cn.tedu.mediaplayer.entity.Music;

public class SearchMusicAdapter extends BaseAdapter{
	private Context context;
	private List<Music> musics;
	private LayoutInflater inflater;
	
	public SearchMusicAdapter(Context context, List<Music> musics) {
		this.context = context;
		this.musics = musics;
		this.inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return musics.size();
	}

	@Override
	public Music getItem(int position) {
		return musics.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if(convertView == null){
			convertView = inflater.inflate(R.layout.item_lv_search_music, null);
			holder = new ViewHolder();
			holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
			holder.tvSinger = (TextView) convertView.findViewById(R.id.tvSinger);
			convertView.setTag(holder);
		}
		holder=(ViewHolder) convertView.getTag();
		//¸ø¿Ø¼þ¸³Öµ
		Music m = getItem(position);
		holder.tvTitle.setText(Html.fromHtml(m.getTitle()));
		holder.tvSinger.setText(Html.fromHtml(m.getAuthor()));
		return convertView;
	}
	
	class ViewHolder{
		TextView tvSinger;
		TextView tvTitle;
	}
	
}


