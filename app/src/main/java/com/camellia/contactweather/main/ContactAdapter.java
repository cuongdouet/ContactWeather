package com.camellia.contactweather.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.camellia.contactweather.R;

import java.util.List;


public class ContactAdapter extends RecyclerView.Adapter<ContactViewHolder> {

  private List<ContactData> myContactList;
  private Context mContext;
  private OnOptionMenuItemClickListener listener;

  public ContactAdapter(List<ContactData> myContactList, Context mContext, OnOptionMenuItemClickListener listener) {
    this.myContactList = myContactList;
    this.mContext = mContext;
    this.listener = listener;
  }

  @NonNull
  @Override
  public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View itemRow = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_list_row, parent, false);
    return new ContactViewHolder(itemRow);
  }

  @Override
  public void onBindViewHolder(@NonNull final ContactViewHolder holder, int position) {
    holder.bind(myContactList.get(position), listener, position);
  }

  @Override
  public int getItemCount() {
    return myContactList.size();
  }


}
