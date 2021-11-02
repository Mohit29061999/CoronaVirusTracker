package io.javabrains.coronavirustracker.services;

import io.javabrains.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;


@Service                      ///it is a spring service
public class CoronaVirusDataService {

    private static String virusDataUrl = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    private List<LocationStats> allStats = new ArrayList<>();
    @PostConstruct         ///after creating this service call this method
    @Scheduled(cron = "* * 1 * * *")                 ///to call this method every second
    public void fetchVirusData() throws IOException, InterruptedException {
         List<LocationStats> newStats = new ArrayList<>();


        HttpClient client = HttpClient.newHttpClient();
       HttpRequest request =  HttpRequest.newBuilder().uri(URI.create(virusDataUrl)).build();     ///creating a request using builder pattern
       HttpResponse<String>  httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());  ///take the body of the url and return it as string

        StringReader csvBodyReader = new StringReader(httpResponse.body());



        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {
            LocationStats locationStats = new LocationStats();
            locationStats.setState(record.get("Province/State"));
            locationStats.setCountry(record.get("Country/Region"));

            locationStats.setLatestTotalCases(Integer.parseInt(record.get(record.size()-1)));

          int latestCases =  Integer.parseInt(record.get(record.size()-1));
            int prevDayCases  =  Integer.parseInt(record.get(record.size()-2));

            locationStats.setLatestTotalCases(latestCases);
            locationStats.setDiffFromPrevDay(latestCases - prevDayCases);

            System.out.println(locationStats);

            newStats.add(locationStats);



        }

        this.allStats = newStats;
    }
}
