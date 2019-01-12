package com.brownietech.aemet_alerts;

import android.os.Handler;
import android.util.Log;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AemetAPI {

    public static final String API_KEY = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZGVsYWNhbGxlQGdtYWlsLmNvbSIsImp0aSI6IjA0YmM5NTNhLTE3YmMtNDkwNS04YTczLWU2YTYwNTUzMTU4ZiIsImlzcyI6IkFFTUVUIiwiaWF0IjoxNTQ2NTA1NzE3LCJ1c2VySWQiOiIwNGJjOTUzYS0xN2JjLTQ5MDUtOGE3My1lNmE2MDU1MzE1OGYiLCJyb2xlIjoiIn0.s9anASRAOO0BW0BlM2awjYeMpQW29YdeH96DvyOTEAw";

    public static void retrieveAvisosFromArea(AemetAPIListener listener, String area) {

        final String urlAPIAemetGetAvisos = "https://opendata.aemet.es/opendata/api/avisos_cap/ultimoelaborado/area/" + area;
        HttpUrl.Builder urlBuilder = HttpUrl.parse(urlAPIAemetGetAvisos)
                .newBuilder();
        String url = urlBuilder
                .build()
                .toString();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("api_key",
                        API_KEY)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("***", "Something very bad was happen retrieving data from Aemet:"+urlAPIAemetGetAvisos);
                e.printStackTrace();
                if(listener!=null) {
                    listener.onError();
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("***", "Something very bad was happen retrieving data from Aemet:"+urlAPIAemetGetAvisos);
                    listener.onError();
                } else {
                    String responseSt = response.body().string();
                    try {
                        JSONObject jsonObject = new JSONObject(responseSt);
                        String datos = jsonObject.getString("datos");

                        Realm realm = Realm.getDefaultInstance();
                        realm.beginTransaction();
                        realm.where(Aviso.class).equalTo("zona" , area).findAll().deleteAllFromRealm();
                        realm.where(Area.class).equalTo("zona" , area).findAll().deleteAllFromRealm();
                        realm.commitTransaction();
                        if(listener!=null) {
                            getAvisos(datos, area, listener);
                        }else{
                            getAvisos(datos, area, null);
                        }
                    }catch (JSONException exception){
                        Log.e("***", "Something very bad was parsing:"+exception.toString());
                        listener.onError();
                    }
                }
            }
        });
    }

    private static void getAvisos(final String datos, final String zona, AemetAPIListener listener) {

        HttpUrl.Builder urlBuilder = HttpUrl.parse(datos)
                .newBuilder();
        String url = urlBuilder
                .build()
                .toString();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("***", "Something very bad was happen retrieving data from Aemet:"+datos);
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful()) {
                    Log.e("***", "Something very bad was happen retrieving data from Aemet:" + datos);
                    throw new IOException("Unexpected code " + response);
                } else {
                    InputStream in = response.body().byteStream();
                    TarArchiveInputStream inStream = new TarArchiveInputStream(in);
                    TarArchiveEntry tarFile;

                    while ((tarFile = (TarArchiveEntry) inStream.getNextEntry()) != null) {
                        Log.e("***", "Tar File:" + tarFile.getName());

                        StringBuilder sb = new StringBuilder();

                        File cf = tarFile.getFile();
                        int size = 0;
                        int c;
                        while (size < tarFile.getSize()) {
                            c = inStream.read();
                            //    Log.e("***", "Tar File:"+(char) c);
                            sb.append((char) c);
                            size++;
                        }
                        if(listener!=null) {
                            parseAndSaveAvisos(sb.toString(), zona, listener);
                        }else{
                            parseAndSaveAvisos(sb.toString(), zona, null);
                        }
                    }

                }
            }
        });
    }

    private static void parseAndSaveAvisos(String s,String zona, AemetAPIListener listener) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {

            String encoded =new String(s.getBytes("ISO-8859-1"),"UTF-8");

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document dom = builder.parse((new InputSource(new StringReader(encoded))));

            Element root = dom.getDocumentElement();

            NodeList nodes = root.getChildNodes();

            Aviso aviso = new Aviso();
            RealmList<Area> areas = new RealmList<>();

            for(int a = 0 ; a < nodes.getLength();a++ ){
                Node node = nodes.item(a);
                if(node.getNodeName().equals("sent")){
                    aviso.setSent(getText(node));
                }
                if(node.getNodeName().equals("msgType")){
                    aviso.setMsgType(getText(node));
                }

            }

            NodeList infos = root.getElementsByTagName("info");

            for(int i =0; i < infos.getLength(); i++){
                Node info = infos.item(i);

                NodeList datosInfo = info.getChildNodes();
                for (int j=0; j<datosInfo.getLength(); j++)
                {
                    Node avisoNode = datosInfo.item(j);
                    if(avisoNode.getNodeName().equals("language")&&!getText(avisoNode).equals("es-ES")) {
                        break;
                    }
                    if (avisoNode.getNodeName().equals("event")) {
                        aviso.setEvent(getText(avisoNode));
                    }
                    if (avisoNode.getNodeName().equals("urgency")) {
                        aviso.setUrgency(getText(avisoNode));
                    }
                    if (avisoNode.getNodeName().equals("severity")) {
                        aviso.setSeverity(getText(avisoNode));
                    }
                    if (avisoNode.getNodeName().equals("certainty")) {
                        aviso.setCertainty(getText(avisoNode));
                    }
                    if (avisoNode.getNodeName().equals("effective")) {
                        aviso.setEffective(getText(avisoNode));
                    }
                    if (avisoNode.getNodeName().equals("onset")) {
                        aviso.setOnset(getText(avisoNode));
                    }
                    if (avisoNode.getNodeName().equals("expires")) {
                        aviso.setExpires(getText(avisoNode));
                    }
                    if (avisoNode.getNodeName().equals("headline")) {
                        aviso.setHeadline(getText(avisoNode));
                    }
                    if (avisoNode.getNodeName().equals("description")) {
                        aviso.setDescription(getText(avisoNode));
                    }
                    if (avisoNode.getNodeName().equals("instruction")) {
                        aviso.setInstruction(getText(avisoNode));
                    }

                    if (avisoNode.getNodeName().equals("eventCode")) {
                        NodeList eventCodeNode = avisoNode.getChildNodes();
                        Node valueName = eventCodeNode.item(1);
                        if(getText(valueName).equals("AEMET-Meteoalerta fenomeno")){
                            aviso.setFenomeno(getText(eventCodeNode.item(3)));
                        }
                    }

                    if (avisoNode.getNodeName().equals("parameter")) {
                        NodeList parametersNode = avisoNode.getChildNodes();
                        Node valueName = parametersNode.item(1);
                        if(getText(valueName).equals("AEMET-Meteoalerta nivel")){
                            aviso.setNivel(getText(parametersNode.item(3)));
                        }
                        if(getText(valueName).equals("AEMET-Meteoalerta parametro")){
                            aviso.setParametro(getText(parametersNode.item(3)));
                        }
                    }



                    if (avisoNode.getNodeName().equals("area")) {

                        Area area = new Area();

                        NodeList areasNode = avisoNode.getChildNodes();

                        Node areaDesc = areasNode.item(1);
                        area.setAreaDesc(getText(areaDesc));
                        Node polygon = areasNode.item(3);
                        area.setPolygon(getText(polygon));
                        Node geocode = areasNode.item(5);

                        area.setZona(zona);


                        NodeList geocodeNodes = geocode.getChildNodes();
                        area.setCode(getText(geocodeNodes.item(3)));

                        Realm realm = Realm.getDefaultInstance();
                        Area areaInDB = realm.where(Area.class).equalTo("code", area.getCode()).findFirst();
                        if(areaInDB == null){
                            realm.beginTransaction();
                            realm.copyToRealm(area);
                            realm.commitTransaction();
                            areas.add(area);

                        }else{
                            areas.add(areaInDB);
                        }
                    }
                }

               // Log.e("***","INFO:"+info.getChildNodes());
            }
            aviso.setAreas(areas);
            aviso.setZona(zona);
            Log.e("***", "AVISO:" + aviso.toString());
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            realm.copyToRealm(aviso);
            realm.commitTransaction();
            realm.refresh();
            realm.close();

                if(listener!=null) {
                    listener.onSuccess();
                }else{

                }
        } catch (ParserConfigurationException e) {
            Log.e("***", "Something very bad was parsing XML from avisos Aemet:"+e.toString());
            e.printStackTrace();
        } catch (SAXException e) {
            Log.e("***", "Something very bad was parsing XML from avisos Aemet SAX:"+e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("***", "Something very bad was parsing XML from avisos Aemet IO:"+e.toString());
            e.printStackTrace();
        }


    }

    private static String getText(Node dato)
    {
        StringBuilder texto = new StringBuilder();
        if(dato!=null) {
            NodeList fragmentos = dato.getChildNodes();
            for (int k = 0; k < fragmentos.getLength(); k++) {
                texto.append(fragmentos.item(k).getNodeValue());
            }
            return texto.toString();
        }else{
            return "";
        }
    }

    public static void retrieveAllAvisos(AemetNotificationsChecker aemetNotificationsChecker) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Alerta> alertas = realm.where(Alerta.class).findAll();

        for(Alerta alerta:alertas){
            retrieveAvisosFromArea(null, alerta.getAreaAviso());
        }

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                aemetNotificationsChecker.onSuccess();
            }
        }, 10000);
    }


    private void tryAPI() {

        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://opendata.aemet.es/opendata/api/avisos_cap/ultimoelaborado/area/70")
                .newBuilder();
        String url = urlBuilder
                .build()
                .toString();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("api_key",
                        "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZGVsYWNhbGxlQGdtYWlsLmNvbSIsImp0aSI6IjA0YmM5NTNhLTE3YmMtNDkwNS04YTczLWU2YTYwNTUzMTU4ZiIsImlzcyI6IkFFTUVUIiwiaWF0IjoxNTQ2NTA1NzE3LCJ1c2VySWQiOiIwNGJjOTUzYS0xN2JjLTQ5MDUtOGE3My1lNmE2MDU1MzE1OGYiLCJyb2xlIjoiIn0.s9anASRAOO0BW0BlM2awjYeMpQW29YdeH96DvyOTEAw")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                String responseSt;
                if (!response.isSuccessful()) {
                    throw new IOException("Unexpected code " + response);
                } else {

                    responseSt = response.body().string();
                    Log.e("***", "RESPONSE:"+ responseSt);

                    try {
                        JSONObject jsonObject = new JSONObject(responseSt);
                        String datos = jsonObject.getString("datos");


                        HttpUrl.Builder urlBuilder = HttpUrl.parse(datos)
                                .newBuilder();
                        String url = urlBuilder
                                .build()
                                .toString();

                        OkHttpClient client = new OkHttpClient();
                        Request request = new Request.Builder()
                                .url(url)
                                //     .addHeader("api_key",
                                //             "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJtZGVsYWNhbGxlQGdtYWlsLmNvbSIsImp0aSI6IjA0YmM5NTNhLTE3YmMtNDkwNS04YTczLWU2YTYwNTUzMTU4ZiIsImlzcyI6IkFFTUVUIiwiaWF0IjoxNTQ2NTA1NzE3LCJ1c2VySWQiOiIwNGJjOTUzYS0xN2JjLTQ5MDUtOGE3My1lNmE2MDU1MzE1OGYiLCJyb2xlIjoiIn0.s9anASRAOO0BW0BlM2awjYeMpQW29YdeH96DvyOTEAw")
                                .build();


                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (!response.isSuccessful()) {
                                    throw new IOException("Unexpected code " + response);
                                } else {
                                    InputStream in = response.body().byteStream();
                                    TarArchiveInputStream inStream = new TarArchiveInputStream(in);
                                    TarArchiveEntry tarFile;

                                    while ((tarFile = (TarArchiveEntry) inStream.getNextEntry()) != null) {
                                        Log.e("***", "Tar File:"+tarFile.getName());

                                        StringBuilder sb = new StringBuilder();

                                        File cf = tarFile.getFile();
                                        int size = 0;
                                        int c;
                                        while (size < tarFile.getSize()) {
                                            c = inStream.read();
                                            //    Log.e("***", "Tar File:"+(char) c);
                                            sb.append((char) c);
                                            size++;
                                        }
                                        Log.e("***", sb.toString());
                                    }

                                }
                            }
                        });




                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }


        });


    }


}
