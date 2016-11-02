package pl.droidcon.app.helper;


import java.util.ArrayList;
import java.util.List;

import pl.droidcon.app.model.api.Speaker;
import pl.droidcon.app.model.db.RealmSpeaker;

public class SpeakerMapper implements Mapper<Speaker, RealmSpeaker> {

    @Override
    public RealmSpeaker map(Speaker speaker) {
        RealmSpeaker realmSpeaker = new RealmSpeaker();
        realmSpeaker.setId(speaker.id);
        realmSpeaker.setFirstName(speaker.firstName);
        realmSpeaker.setLastName(speaker.lastName);
        realmSpeaker.setBio(speaker.bio);
        realmSpeaker.setImageUrl(speaker.imageUrl);
        realmSpeaker.setWebsiteTitle(speaker.websiteTitle);
        realmSpeaker.setWebsiteLink(speaker.websiteLink);
        realmSpeaker.setFacebookLink(speaker.facebookLink);
        realmSpeaker.setTwitterHandler(speaker.twitterHandler);
        realmSpeaker.setGithubLink(speaker.githubLink);
        realmSpeaker.setLinkedIn(speaker.linkedIn);
        realmSpeaker.setGooglePlus(speaker.googlePlus);
        return realmSpeaker;
    }

    @Override
    public List<Speaker> fromDBList(List<RealmSpeaker> realmSpeakers) {
        List<Speaker> speakers = new ArrayList<>();

        for (RealmSpeaker realmSpeaker : realmSpeakers) {
            speakers.add(fromDB(realmSpeaker));
        }

        return speakers;
    }

    @Override
    public Speaker fromDB(RealmSpeaker realmSpeaker) {
        Speaker speaker = new Speaker();
        speaker.id = realmSpeaker.getId();
        speaker.firstName = realmSpeaker.getFirstName();
        speaker.lastName = realmSpeaker.getLastName();
        speaker.bio = realmSpeaker.getBio();
        speaker.imageUrl = realmSpeaker.getImageUrl();
        speaker.websiteTitle = realmSpeaker.getWebsiteTitle();
        speaker.websiteLink = realmSpeaker.getWebsiteLink();
        speaker.facebookLink = realmSpeaker.getFacebookLink();
        speaker.twitterHandler = realmSpeaker.getTwitterHandler();
        speaker.githubLink = realmSpeaker.getGithubLink();
        speaker.linkedIn = realmSpeaker.getLinkedIn();
        speaker.googlePlus = realmSpeaker.getGooglePlus();
        return speaker;
    }
}
