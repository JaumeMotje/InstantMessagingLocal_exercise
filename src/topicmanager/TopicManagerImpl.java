package topicmanager;

import util.Subscription_check;
import util.Topic;
import util.Topic_check;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import publisher.Publisher;
import publisher.PublisherImpl;
import subscriber.Subscriber;

public class TopicManagerImpl implements TopicManager {

  private Map<Topic, Publisher> topicMap;

  public TopicManagerImpl() {
    topicMap = new HashMap<Topic, Publisher>();
  }

  @Override
  public Publisher addPublisherToTopic(Topic topic) {
      if (topicMap.containsKey(topic)) {
        return topicMap.get(topic); // Return the existing publisher
    }
    
    Publisher publisher = new PublisherImpl(topic);
    topicMap.put(topic, publisher);
    return publisher;
  }

  @Override
  public void removePublisherFromTopic(Topic topic) {
       if (topicMap.containsKey(topic)) {
           topicMap.remove(topic);   
    }
  }

  @Override
  public Topic_check isTopic(Topic topic) {
    boolean check = topicMap.containsKey(topic);
    return new Topic_check(topic,check);
  }

  @Override
  public List<Topic> topics() {
    return new ArrayList<Topic>(topicMap.keySet());
  }

  @Override
  public Subscription_check subscribe(Topic topic, Subscriber subscriber) {
    if (!topicMap.containsKey(topic)) {
        return new Subscription_check(topic, Subscription_check.Result.NO_TOPIC);
    }
    Publisher publisher = topicMap.get(topic);
    if (publisher == null) {
        return new Subscription_check(topic, Subscription_check.Result.NO_SUBSCRIPTION);
    }
    publisher.attachSubscriber(subscriber);
    return new Subscription_check(topic, Subscription_check.Result.OKAY);
  }

  @Override
  public Subscription_check unsubscribe(Topic topic, Subscriber subscriber) {
    if (!topicMap.containsKey(topic)) {
        return new Subscription_check(topic, Subscription_check.Result.NO_TOPIC);
    }
    Publisher publisher = topicMap.get(topic);
    if (publisher == null) {
        return new Subscription_check(topic, Subscription_check.Result.NO_SUBSCRIPTION);
    }
    publisher.detachSubscriber(subscriber);
    return new Subscription_check(topic, Subscription_check.Result.OKAY);
  }

  
}
