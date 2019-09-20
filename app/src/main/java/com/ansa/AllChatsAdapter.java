package com.ansa;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class AllChatsAdapter extends RecyclerView.Adapter<AllChatsAdapter.PersonViewHolder> {

    public static class PersonViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView userName;
        TextView counter;
        public View container;


        PersonViewHolder(final View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            userName = (TextView)itemView.findViewById(R.id.user_name_text_view);
            counter = (TextView)itemView.findViewById(R.id.counter_text_view);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   /* String phone = ads.get(getAdapterPosition()).getPhone();
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                    itemView.getContext().startActivity(intent);*/

                }
            });
        }
    }

    static List<Chat> chats;

    AllChatsAdapter(List<Chat> chats){
        this.chats = chats;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public PersonViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.all_chats_item, viewGroup, false);
        PersonViewHolder pvh = new PersonViewHolder(v);
        return pvh;
    }

    @Override
    public void onBindViewHolder(PersonViewHolder personViewHolder, int i) {
        personViewHolder.userName.setText(chats.get(i).getUsername());
        personViewHolder.counter.setText(chats.get(i).getCounter());

    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

}