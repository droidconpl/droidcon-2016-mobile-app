package pl.droidcon.app.http;

import java.util.List;

import pl.droidcon.app.model.api.AgendaResponse;
import pl.droidcon.app.model.api.AgendaRow;
import pl.droidcon.app.model.db.SpeakerEntity;
import retrofit.http.GET;
import rx.Observable;

public interface RestService {

    @GET("/model/agenda.json")
    Observable<AgendaResponse> getAgenda();

    @GET("/model/speakers.json")
    Observable<List<SpeakerEntity>> getSpeakers();

    @GET("/model/agenda.json")
    Observable<List<AgendaRow>> getData();
}
