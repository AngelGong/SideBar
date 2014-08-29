package com.gong.sidebar;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 
 * @author lfgong
 *
 */
public class DorisSideBar extends LinearLayout{

	private static SideItemClickEvent itemClickEvent = null;
	private static Activity mContext = null;
	private static List<NavBarItem> defaultItems = null;
	private static List<NavBarItem> items = null;
	private static ListView listView = null;
	private static final Object obj = new Object();
	private static ImageView image = null;
	private static PopupWindow popWindow = null;
	//保证只有一个被初始化的侧边栏
	private static DorisSideBar sidebar = null;
	/**
	 * 初始化一个侧边栏
	 * @param context
	 */
	DorisSideBar(Context context) {
		super(context);
		//设置ListView的layout参数
		initListView();
		initImageView();
		this.addView(image);
		this.addView(listView);
		this.setGravity(Gravity.TOP|Gravity.RIGHT);
		this.setOrientation(LinearLayout.VERTICAL);
		this.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				dismissPopWindow();
				return false;
			}
		});
	}
	
	/**
	 * 初始化ListView
	 */
	private void initListView()
	{
		listView = new ListView(mContext);
		LayoutParams param = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		listView.setLayoutParams(param);
		listView.setAdapter(new SideBarListAdapter());
		listView.setCacheColorHint(android.R.color.transparent);
		listView.setBackground(getResources().getDrawable(R.drawable.sidebar_bg_shape_263846));
		listView.setAlpha(0.9f);
		listView.setScrollingCacheEnabled(false);
		listView.setDivider(getResources().getDrawable(R.color.sidebar_slide_color));
		listView.setDividerHeight(1);
	}
	
	private void initImageView()
	{
		image = new ImageView(mContext);
		image.setImageResource(R.drawable.bg_sidebar);
		image.setAlpha(0.95f);
		image.setPadding(0, 0,  getpxFromPd(4.5f),  getpxFromPd(-1.0f));
	}
	
	private static int getpxFromPd(float dip)
	{
		return (int)(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dip, mContext.getResources().getDisplayMetrics()) + 0.5f);
	}
	
	private static void initPopWindow()
	{
		popWindow = new PopupWindow(sidebar, getpxFromPd(156.0f), LayoutParams.WRAP_CONTENT, true);
		popWindow.setAnimationStyle(R.style.style_anim_sidebar);
		popWindow.setOutsideTouchable(true);
		popWindow.setTouchable(true);
		popWindow.setFocusable(true);
		popWindow.showAtLocation(mContext.getWindow().getDecorView(), Gravity.TOP|Gravity.RIGHT, getpxFromPd(5.0f), getpxFromPd(64.0f));
	}
	
	private static void dismissPopWindow()
	{
		if(popWindow != null)
		{
			popWindow.dismiss();
			popWindow = null;
		}
	}
	
	/**
	 * load 侧边栏，线程安全
	 * @param items
	 * @param context
	 * @param clickEvent
	 * @return 返回侧边栏是否显示，true显示 false不显示
	 */
	public static boolean loadDorisSideBar(List<NavBarItem> items, Activity context, SideItemClickEvent clickEvent)
	{
		if(sidebar != null && context.equals(mContext))  //如果是同一个Activity引用在加载当前SideBar
		{
			initPopWindow();
			return true;
		}
		else
		{
			if(clickEvent == null) return false;
			synchronized (obj) {
				itemClickEvent = clickEvent;
				mContext = context;
				DorisSideBar.items = items;
				sidebar = new DorisSideBar(context);
				initPopWindow();
			}
		}
		return true;
	}
	
	ListView.OnClickListener mListener = new ListView.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			TextView title = (TextView) v.findViewById(R.id.sidebar_item_title);
			dismissPopWindow();
			Toast toast = Toast.makeText(mContext, title.getContentDescription().toString(), 200);
			toast.show();
			if(itemClickEvent != null)
				itemClickEvent.navigateTo(title.getContentDescription().toString());
		}
	};
	
	class SideBarListAdapter extends BaseAdapter
	{

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return items.get(position);
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TextView title = null;
			ImageView img = null;
			if(convertView == null || position <= items.size())
			{
				convertView = LayoutInflater.from(mContext).inflate(R.layout.sidebar_list_item,null);
				title = (TextView) convertView.findViewById(R.id.sidebar_item_title);
				img = (ImageView) convertView.findViewById(R.id.sidebar_item_img);
			}
			NavBarItem item = items.get(position);
			convertView.setOnClickListener(mListener);
			title.setText(items.get(position).title);
			title.setContentDescription(item.tagName);
			img.setImageBitmap(BitmapFactory.decodeResource(getResources(), item.imageResId));
			return convertView;
		}
		
	}

	public interface SideItemClickEvent{
		void navigateTo(String tagName);
	}
	
	public interface OnVisibilityChangeListener {
		/**
		 * Called when the system UI visibility has changed.
		 * 
		 * @param visible
		 *            True if the system UI is visible.
		 */
		public void onVisibilityChange(boolean visible);
	}
	/**
	 * 根据tagName返回默认列表中的Item
	 * @param tagName
	 * @return 返回默认列表中TagName对应的Item，没有则返回null
	 */
	public static NavBarItem getItemByTagName(String tagName){
		for(NavBarItem item : defaultItems)
		{
			if(tagName.equals(item.tagName))
				return item;
		}
		return null;
	}
	/**
	 * 返回默认侧边栏配置信息，如果默认侧边栏信息没有初始化，会在这里初始化
	 * @return
	 */
	public static List<NavBarItem> getDefalutItems()
	{
		if(defaultItems == null)
			initDefaultItems();
		return defaultItems;
	}

	/**
	 * 初始化默认显示选项
	 * @return
	 */
	private static void initDefaultItems()
	{
		defaultItems = new ArrayList<NavBarItem>();
		//-------------------
		NavBarItem item = new NavBarItem();
		item.title = "我的收藏";
		item.tagName = "favorite";
		item.imageResId = R.drawable.ico_sidebar_favourite;
		defaultItems.add(item);
		//-------------------
		item = new NavBarItem();
		item.title = "我的订单";
		item.tagName = "orders";
		item.imageResId = R.drawable.ico_sidebar_order;
		defaultItems.add(item);
		//-------------------
		item = new NavBarItem();
		item.title = "消息中心";
		item.tagName = "message";
		item.imageResId = R.drawable.ico_sidebar_message;
		defaultItems.add(item);
		//-------------------
		item = new NavBarItem();
		item.title = "回到首页";
		item.tagName = "home";
		item.imageResId = R.drawable.ico_sidebar_home;
		defaultItems.add(item);
	}         
}
