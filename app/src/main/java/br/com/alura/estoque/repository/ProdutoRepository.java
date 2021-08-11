package br.com.alura.estoque.repository;

import android.content.Context;

import java.util.List;

import br.com.alura.estoque.asynctask.BaseAsyncTask;
import br.com.alura.estoque.database.EstoqueDatabase;
import br.com.alura.estoque.database.dao.ProdutoDAO;
import br.com.alura.estoque.model.Produto;
import br.com.alura.estoque.retrofit.EstoqueRetrofit;
import br.com.alura.estoque.retrofit.callback.CallbackComRetorno;
import br.com.alura.estoque.retrofit.callback.CallbackSemRetorno;
import br.com.alura.estoque.retrofit.service.ProdutoService;
import retrofit2.Call;

public class ProdutoRepository {

    private final ProdutoDAO dao;
    private final ProdutoService service;

    public ProdutoRepository(Context context) {
        EstoqueDatabase db = EstoqueDatabase.getInstance(context);
        dao = db.getProdutoDAO();
        service = new EstoqueRetrofit().getProdutoService();
    }

    public void buscaProdutos(DadosCarregadosCallback<List<Produto>> callback) {
        buscaProdutosInternos(callback);
    }

    private void buscaProdutosInternos(DadosCarregadosCallback<List<Produto>> callback) {
        new BaseAsyncTask<>(dao::buscaTodos,
                resultado -> {
//                  Notifica p/ activity que o dado está pronto!
                    callback.quandoSucesso(resultado);
                    buscaProdutosNaApi(callback);
                }).execute();
    }

    private void buscaProdutosNaApi(DadosCarregadosCallback<List<Produto>> callback) {
        Call<List<Produto>> call = service.buscaTodos();
        call.enqueue(new CallbackComRetorno<>(new CallbackComRetorno.RespostaCallback<List<Produto>>() {
            @Override
            public void quandoSucesso(List<Produto> produtosNovos) {
                atualizaInterno(produtosNovos, callback);
            }

            @Override
            public void quandoFalha(String erro) {
                callback.quandoFalha(erro);
            }
        }));
    }

//                new Callback<List<Produto>>() {
//            @Override
//            @EverythingIsNonNull
//            public void onResponse(Call<List<Produto>> call, Response<List<Produto>> response) {
//                if (response.isSuccessful()) {
//                    List<Produto> produtosNovos = response.body();
//                    if (produtosNovos != null) {
//                        atualizaInterno(produtosNovos, callback);
//                    }
//                } else {
//                    callback.quandoFalha("Falha de comunicação");
//                }
//            }
//            @Override
//            public void onFailure(Call<List<Produto>> call, Throwable t) {
//                callback.quandoFalha("Falha de comunicação: " + t.getMessage());
//            }
//        }


    private void atualizaInterno(List<Produto> produtosNovos,
                                 DadosCarregadosCallback<List<Produto>> callback) {
        new BaseAsyncTask<>(() -> {
            dao.salva(produtosNovos);
            return dao.buscaTodos();
        }, callback::quandoSucesso)
                .execute();
    }
//      ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//        new BaseAsyncTask<>(() -> {
//            try {
//                Response<List<Produto>> resposta = call.execute();
//                List<Produto> produtosNovos = resposta.body();
//                dao.salva(produtosNovos);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            return dao.buscaTodos();
//        }, //  Notifica p/ activity que o dado está pronto!
//                callback::quandoCarregados)
//                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//    }

    public void salva(Produto produto,
                      DadosCarregadosCallback<Produto> callback) {
        salvaNaApi(produto, callback);
    }

    private void salvaNaApi(Produto produto,
                            DadosCarregadosCallback<Produto> callback) {
        Call<Produto> call = service.salva(produto);
        call.enqueue(new CallbackComRetorno<>(new CallbackComRetorno.RespostaCallback<Produto>() {
            @Override
            public void quandoSucesso(Produto produtoSalvo) {
                salvaInterno(produtoSalvo, callback);
            }

            @Override
            public void quandoFalha(String erro) {
                callback.quandoFalha(erro);
            }
        }));
    }

//                new Callback<Produto>() {
//            @Override
//            @EverythingIsNonNull
//            public void onResponse(Call<Produto> call,
//                                   Response<Produto> response) {
//                if (response.isSuccessful()) {
//                    Produto produtoSalvo = response.body();
//                    if (produtoSalvo != null) {
//                        salvaInterno(produtoSalvo, callback);
//                    }
//                } else{
//                // Notificar uma falha
//                    callback.quandoFalha("Resposta não sucedida");
//                }
//            }
//            @Override
//            @EverythingIsNonNull
//            public void onFailure(Call<Produto> call,
//                                  Throwable t) {
//                // Notificar falha
//                callback.quandoFalha("Falha de comunicação: " + t.getMessage());
//            }
//        }

    private void salvaInterno(Produto produto,
                              DadosCarregadosCallback<Produto> callback) {
        new BaseAsyncTask<>(() -> {
            long id = dao.salva(produto);
            return dao.buscaProduto(id);
        }, //Notificar p/ activity que o dado está pronto!
                callback::quandoSucesso)
                .execute();
    }

    public void edita(Produto produto,
                      DadosCarregadosCallback<Produto> callback) {
        // Criando requisição
        editaNaApi(produto, callback);
    }

    private void editaNaApi(Produto produto, DadosCarregadosCallback<Produto> callback) {
        Call<Produto> call = service.edita(produto.getId(), produto);
        call.enqueue(new CallbackComRetorno<>(
                new CallbackComRetorno.RespostaCallback<Produto>() {
            @Override
            public void quandoSucesso(Produto resultado) {
                editaInterno(produto, callback);
            }

            @Override
            public void quandoFalha(String erro) {
                callback.quandoFalha(erro);
            }
        }));
    }

    private void editaInterno(Produto produto,
                              DadosCarregadosCallback<Produto> callback) {
        new BaseAsyncTask<>(() -> {
            dao.atualiza(produto);
            return produto;
        }, callback::quandoSucesso)
                .execute();
    }

    public void remove(Produto produto,
                       DadosCarregadosCallback<Void> callback) {
        removeNaApi(produto, callback);
    }

    private void removeNaApi(Produto produto,
                             DadosCarregadosCallback<Void> callback) {
        Call<Void> call = service.remove(produto.getId());
        call.enqueue(new CallbackSemRetorno(
                new CallbackSemRetorno.RespostaCallback() {
                    @Override
                    public void quandoSucesso() {
                        removeInterno(produto, callback);
                    }

                    @Override
                    public void quandoFalha(String erro) {
                        callback.quandoFalha(erro);
                    }
                }));
    }

    private void removeInterno(Produto produto,
                               DadosCarregadosCallback<Void> callback) {
        new BaseAsyncTask<>(() -> {
            dao.remove(produto);
            return null;
        }, callback::quandoSucesso)
                .execute();
    }

    public interface DadosCarregadosCallback<T> {
        void quandoSucesso(T resultado);

        void quandoFalha(String erro);
    }

}
