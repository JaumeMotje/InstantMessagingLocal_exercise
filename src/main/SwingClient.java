package main;

import util.Message;
import util.Subscription_check;
import util.Topic;
import subscriber.SubscriberImpl;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import publisher.Publisher;
import publisher.PublisherImpl;
import subscriber.Subscriber;
import topicmanager.TopicManager;
import util.Topic_check;

public class SwingClient {

  TopicManager topicManager;
  public Map<Topic, Subscriber> my_subscriptions;
  public Map<Topic, Publisher> my_publishers; // Store publishers for multiple topics
  Topic activePublisherTopic;                // Currently selected topic for publishing

  JFrame frame;
  JTextArea topic_list_TextArea;
  public JTextArea messages_TextArea;
  public JTextArea my_subscriptions_TextArea;
  JComboBox<Topic> publisherComboBox;        // Dropdown to select active topic
  JTextField argument_TextField;

  public SwingClient(TopicManager topicManager) {
    this.topicManager = topicManager;
    my_subscriptions = new HashMap<Topic, Subscriber>();
    my_publishers = new HashMap<Topic, Publisher>();
    activePublisherTopic = null;
  }


  public void createAndShowGUI() {

    frame = new JFrame("Publisher/Subscriber demo");
    frame.setSize(300, 300);
    frame.addWindowListener(new CloseWindowHandler());

    topic_list_TextArea = new JTextArea(5, 10);
    messages_TextArea = new JTextArea(10, 20);
    messages_TextArea.setEditable(false);
    messages_TextArea.setLineWrap(true);
    messages_TextArea.setWrapStyleWord(true);
    my_subscriptions_TextArea = new JTextArea(5, 10);
    publisherComboBox = new JComboBox<Topic>();
    argument_TextField = new JTextField(20);

    JButton show_topics_button = new JButton("show Topics");
    JButton new_publisher_button = new JButton("new Publisher");
    JButton new_subscriber_button = new JButton("new Subscriber");
    JButton to_unsubscribe_button = new JButton("to unsubscribe");
    JButton to_post_an_event_button = new JButton("post an event");
    JButton forward_message_button = new JButton("forward Message");
    JButton to_close_the_app = new JButton("close app.");

    show_topics_button.addActionListener(new showTopicsHandler());
    new_publisher_button.addActionListener(new newPublisherHandler());
    new_subscriber_button.addActionListener(new newSubscriberHandler());
    to_unsubscribe_button.addActionListener(new UnsubscribeHandler());
    to_post_an_event_button.addActionListener(new postEventHandler());
    forward_message_button.addActionListener(new ForwardMessageHandler());
    to_close_the_app.addActionListener(new CloseAppHandler());

    JPanel buttonsPannel = new JPanel(new FlowLayout());
    buttonsPannel.add(show_topics_button);
    buttonsPannel.add(new_publisher_button);
    buttonsPannel.add(new_subscriber_button);
    buttonsPannel.add(to_unsubscribe_button);
    buttonsPannel.add(to_post_an_event_button);
    buttonsPannel.add(forward_message_button);
    buttonsPannel.add(to_close_the_app);

    JPanel argumentP = new JPanel(new FlowLayout());
    argumentP.add(new JLabel("Write content to set a new_publisher / new_subscriber / unsubscribe / post_event:"));
    argumentP.add(argument_TextField);

    publisherComboBox = new JComboBox<Topic>();
    publisherComboBox.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            activePublisherTopic = (Topic) publisherComboBox.getSelectedItem();
            if (activePublisherTopic != null) {
                messages_TextArea.append("Switched to publishing on topic: " + activePublisherTopic.name + "\n");
            }
        }
    });
    
    JPanel topicsP = new JPanel();
    topicsP.setLayout(new BoxLayout(topicsP, BoxLayout.PAGE_AXIS));
    topicsP.add(new JLabel("Topics:"));
    topicsP.add(topic_list_TextArea);
    topicsP.add(new JScrollPane(topic_list_TextArea));
    topicsP.add(new JLabel("My Subscriptions:"));
    topicsP.add(my_subscriptions_TextArea);
    topicsP.add(new JScrollPane(my_subscriptions_TextArea));
    topicsP.add(new JLabel("I'm Publisher of topics:"));
    topicsP.add(publisherComboBox); // Use ComboBox to switch topics

    JPanel messagesPanel = new JPanel();
    messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.PAGE_AXIS));
    messagesPanel.add(new JLabel("Messages:"));
    messagesPanel.add(messages_TextArea);
    messagesPanel.add(new JScrollPane(messages_TextArea));

    Container mainPanel = frame.getContentPane();
    mainPanel.add(buttonsPannel, BorderLayout.PAGE_START);
    mainPanel.add(messagesPanel, BorderLayout.CENTER);
    mainPanel.add(argumentP, BorderLayout.PAGE_END);
    mainPanel.add(topicsP, BorderLayout.LINE_START);

    frame.pack();
    frame.setVisible(true);
  }

  class showTopicsHandler implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      
        ArrayList<Topic> topicsList = (ArrayList) topicManager.topics();
        topic_list_TextArea.setText("");
        for(Topic topic : topicsList){
            topic_list_TextArea.append(topic.name + "\n");
        }
    }
  }

  class newPublisherHandler implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        String topicName = argument_TextField.getText().trim();

        if (topicName.isEmpty()) {
            messages_TextArea.append("Error: Topic name cannot be empty.\n");
            return;
        }

        Topic selectedTopic = new Topic(topicName);

        // Check if the topic already exists
        if (!topicManager.topics().contains(selectedTopic)) {
            messages_TextArea.append("Topic '" + topicName + "' does not exist. Creating a new topic.\n");
            topicManager.topics().add(selectedTopic); // Add the topic to the manager
        } else {
            messages_TextArea.append("Topic '" + topicName + "' already exists. Assigning you as a publisher.\n");
        }

        // Create a publisher for the topic
        Publisher newPublisher = topicManager.addPublisherToTopic(selectedTopic);
        if (newPublisher != null) {
            my_publishers.put(selectedTopic, newPublisher);         
            
            DefaultComboBoxModel<Topic> model = (DefaultComboBoxModel<Topic>) publisherComboBox.getModel();
            boolean topicExists = false;

            // Iterate through the ComboBox items to check if the topic is already there
            for (int i = 0; i < model.getSize(); i++) {
                if (model.getElementAt(i).equals(selectedTopic)) {
                    topicExists = true;
                    break;
                }
            }

            // Add the topic only if it doesn't already exist
            if (!topicExists) {
                publisherComboBox.addItem(selectedTopic);
            }
            
            activePublisherTopic = selectedTopic;

            // Treat the publisher as a subscriber to receive messages
            // topicManager.subscribe(selectedTopic, (Subscriber) newPublisher);

            messages_TextArea.append("You are now a publisher for topic: " + topicName + "\n");
        } else {
            messages_TextArea.append("Error: Failed to assign you as a publisher for topic: " + topicName + "\n");
        }
    }
}

  class newSubscriberHandler implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        String topicName = argument_TextField.getText().trim();
        Topic topic = new Topic(topicName);
        Subscriber subscriber = new SubscriberImpl(SwingClient.this);
        Subscription_check result = topicManager.subscribe(topic, subscriber);
        if (result.result == Subscription_check.Result.OKAY) {
            my_subscriptions.put(topic, subscriber); 
            my_subscriptions_TextArea.setText("");
            for (Topic t : my_subscriptions.keySet()) {
                my_subscriptions_TextArea.append(t.name + "\n");
            }
            messages_TextArea.append("Successfully subscribed to topic: " + topicName + "\n");
        } else if (result.result == Subscription_check.Result.NO_TOPIC) {
            messages_TextArea.append("Error: Topic '" + topicName + "' does not exist.\n");
        }
      
    }
  }

  class UnsubscribeHandler implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      
      String topicName = argument_TextField.getText().trim();
      Topic topic = new Topic(topicName);
       Subscriber subscriber = my_subscriptions.get(topic);
       if (subscriber == null) {
            messages_TextArea.append("Error: You are not subscribed to topic '" + topicName + "'.\n");
            return;
       }

        Subscription_check result = topicManager.unsubscribe(topic, subscriber);

        if (result.result == Subscription_check.Result.OKAY) {
            my_subscriptions.remove(topic); 
            
            my_subscriptions_TextArea.setText("");
            for (Topic t : my_subscriptions.keySet()) {
                my_subscriptions_TextArea.append(t.name + "\n");
            }

            messages_TextArea.append("Successfully unsubscribed from topic: " + topicName + "\n");
        } else if (result.result == Subscription_check.Result.NO_TOPIC) {
            messages_TextArea.append("Error: Topic '" + topicName + "' does not exist.\n");
        }
      
    }
  }

  class postEventHandler implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        if (activePublisherTopic == null) {
            messages_TextArea.append("Error: No topic selected for publishing.\n");
            return;
        }

        Publisher publisher = my_publishers.get(activePublisherTopic);
        if (publisher == null) {
            messages_TextArea.append("Error: No publisher found for the selected topic.\n");
            return;
        }

        String content = argument_TextField.getText();
        Message message = new Message(activePublisherTopic, content);
        publisher.publish(message);

        messages_TextArea.append("Message posted to topic '" + activePublisherTopic.name + "': " + content + "\n");
    }
  }
  
  class ForwardMessageHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
        if (activePublisherTopic == null) {
            messages_TextArea.append("Error: No topic selected for publishing.\n");
            return;
        }

        // Verify that there is a message selected
        String selectedMessage = messages_TextArea.getSelectedText();
        if (selectedMessage == null || selectedMessage.trim().isEmpty()) {
            messages_TextArea.append("Error: No message selected for forwarding.\n");
            return;
        }

        Publisher publisher = my_publishers.get(activePublisherTopic);
        if (publisher == null) {
            messages_TextArea.append("Error: No publisher found for the selected topic.\n");
            return;
        }

        // Create a message and publish it
        Message message = new Message(activePublisherTopic, selectedMessage);
        publisher.publish(message);

        messages_TextArea.append("Message forwarded to topic '" + activePublisherTopic.name + "': " + selectedMessage + "\n");
    }
}


  class CloseAppHandler implements ActionListener {

    public void actionPerformed(ActionEvent e) {
      System.out.println("all users closed");
      System.exit(0);
    }
  }

  class CloseWindowHandler implements WindowListener {

    public void windowDeactivated(WindowEvent e) {
    }

    public void windowActivated(WindowEvent e) {
    }

    public void windowIconified(WindowEvent e) {
    }

    public void windowDeiconified(WindowEvent e) {
    }

    public void windowClosed(WindowEvent e) {
    }

    public void windowOpened(WindowEvent e) {
    }

    public void windowClosing(WindowEvent e) {
      
      //...
      
      System.out.println("one user closed");
    }
  }
}
