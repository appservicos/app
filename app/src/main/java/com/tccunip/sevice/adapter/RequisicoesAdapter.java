package com.tccunip.sevice.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tccunip.sevice.R;
import com.tccunip.sevice.model.Requisicao;
import com.tccunip.sevice.model.Usuario;

import java.util.List;

public class RequisicoesAdapter extends RecyclerView.Adapter<RequisicoesAdapter.MyViewHolder> {

    private List<Requisicao> requisicaos;
    private Context context;
    private Usuario prestador;

    public RequisicoesAdapter(List<Requisicao> requisicaos, Context context, Usuario prestador) {
        this.requisicaos = requisicaos;
        this.context = context;
        this.prestador = prestador;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View item = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.adapter_requisicoes, viewGroup, false);
        return new MyViewHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Requisicao requisicao = requisicaos.get(i);
        Usuario cliente = requisicao.getCliente();

        myViewHolder.nome.setText(cliente.getNome());
        myViewHolder.distancia.setText("1 km - aproximadamente");
    }

    @Override
    public int getItemCount() {
        return requisicaos.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{
        TextView nome, distancia;

        public MyViewHolder(View itemView) {
            super(itemView);

            nome = itemView.findViewById(R.id.textoRequisicaoNome);
            distancia = itemView.findViewById(R.id.textoRequisicaoDistancia);
        }
    }
}
