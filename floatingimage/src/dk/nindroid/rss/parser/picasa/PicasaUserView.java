package dk.nindroid.rss.parser.picasa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import dk.nindroid.rss.R;
import dk.nindroid.rss.settings.Settings;

public class PicasaUserView extends ListFragment {
	private static final int	STREAM		 		= 0;
	private static final int	ALBUMS				= 1;
	
	String id;
	
	public static PicasaUserView getInstance(String id){
		PicasaUserView puv = new PicasaUserView();
		
		Bundle b = new Bundle();
		b.putString("ID", id);
		puv.setArguments(b);
		
		return puv;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		id = getArguments().getString("ID");
		fillMenu();
	}
	
	private void fillMenu(){
		String photosOf = this.getResources().getString(R.string.picasaShowStream);
		String albums = this.getResources().getString(R.string.picasaShowAlbums);
		String[] options = new String[]{photosOf, albums};
		setListAdapter(new ArrayAdapter<String>(this.getActivity(), android.R.layout.simple_list_item_1, options));
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		FrameLayout fl = new FrameLayout(this.getActivity());
		final EditText input = new EditText(this.getActivity());

		fl.addView(input, FrameLayout.LayoutParams.FILL_PARENT);
		input.setGravity(Gravity.CENTER);
		switch(position){
		case STREAM:
			returnStream();
			break;
		case ALBUMS:
			showAlbums();
			break;
		}
	}

	private void returnStream() {
		String url = null;
		url = PicasaFeeder.getRecent(id);
		if(url != null){
			returnResult(url, "Photos of " + id);
		}
	}
	
	private void showAlbums() {
		Intent intent = new Intent(this.getActivity(), PicasaAlbumBrowser.class);
		intent.putExtra(PicasaAlbumBrowser.OWNER, id);
		startActivityForResult(intent, ALBUMS);
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			switch(requestCode){
			case ALBUMS:
				this.getActivity().setResult(Activity.RESULT_OK, data);
				this.getActivity().finish();
				break;
			}
		}
	}
	
	private void returnResult(String url, String name){
		Intent intent = new Intent();
		Bundle b = new Bundle();
		
		b.putString("PATH", url);
		b.putString("NAME", name);
		b.putInt("TYPE", Settings.TYPE_PICASA);
		intent.putExtras(b);
		this.getActivity().setResult(Activity.RESULT_OK, intent);		
		this.getActivity().finish();
	}
}
