import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

/**
 *
 * @author gabriel
 */
public class HTTPServer {
  Socket socket;
  BufferedReader in;
  OutputStream out;
  ServerSocket ss;
  String requestClient = "";
  String caminhoArquivo = "";
  String protocolo = "";
  String pathSite = "";

  public void setup() {
    /* cria um socket "servidor" associado a porta 6789 já aguardando conexões */
    try {
      ss = new ServerSocket(6789);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void waitClient() {
    try {
      // aceita a primeita conexao que vier
      socket = ss.accept();
      /// cria um BufferedReader a partir do InputStream do cliente
      in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      // cria o canal de resposta utilizando o outputStream
      out = socket.getOutputStream();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void receiveRequest() {
    String line = "";
    try {
      /* Lê a primeira linha contem as informaçoes da requisição */
      if ((line = in.readLine()).length() > 0) {

        requestClient = line;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
    // quebra a string pelo espaço em branco
    String[] dadosReq = requestClient.split(" ");
    // paga o caminho do arquivo
    caminhoArquivo = dadosReq[1];
    // pega o protocolo
    protocolo = dadosReq[2];

  }

  /**
   * @throws IOException
   */
  public void sendReply() throws IOException {
    String tipo = "";
    // define o Content-Type do cabeçalho
    if (caminhoArquivo.contains(".jpg")) {
      tipo = "image/jpg";
    } else if (caminhoArquivo.contains(".png")) {
      tipo = "image/png";
    } else if (caminhoArquivo.contains(".html")) {
      tipo = "image/html";
    } else if (caminhoArquivo.contains(".jpeg")) {
      tipo = "image/jpg";
    }

    // define qual arquivo é aberto conforme o href oriundo do html
    if (caminhoArquivo.equals("/")) {
      caminhoArquivo = "index.html";
    } else if (caminhoArquivo.equals("/pagina1")) {
      caminhoArquivo = "pagina1.html";
    } else if (caminhoArquivo.equals("/pagina2")) {
      caminhoArquivo = "pagina2.html";
    } else if (caminhoArquivo.equals("/pagina3")) {
      caminhoArquivo = "pagina3.html";
    }

    // abre o arquivo pelo caminho
    File arquivo = new File(caminhoArquivo.replaceFirst("/", ""));

    String status = protocolo + " 200 OK\r\n";
    // se o arquivo não existe então abrimos o arquivo de erro, e mudamos o status
    // para 404
    if (!arquivo.exists()) {
      status = protocolo + " 404 Not Found\r\n";
      arquivo = new File("404erro.html");
    }

    try {
      // lê todo o conteúdo do arquivo para bytes
      byte[] conteudo = Files.readAllBytes(arquivo.toPath());
      // cabeçalho padrão da resposta HTTP
      String data = status
          + "Server: MeuServidor/1.0\r\n"
          + "Content-Type: " + tipo + "\r\n"
          + "Content-Length: " + conteudo.length + "\r\n"
          + "Connection: close\r\n"
          + "\r\n";

      // escreve o headers em bytes
      out.write(data.getBytes());
      // escreve o conteudo em bytes
      out.write(conteudo);
      // encerra a resposta
      out.flush();
    } catch (Exception e) {
      // TODO: handle exception
    }
  }

  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) throws IOException {

    // instância do server
    HTTPServer server = new HTTPServer();
    // define a porta de comunicação
    server.setup();
    while (true) {
      // cria os canais de resposta e requisição
      server.waitClient();
      // escuta das requisições
      server.receiveRequest();
      // retorno as requisições
      server.sendReply();
    }
  }

}