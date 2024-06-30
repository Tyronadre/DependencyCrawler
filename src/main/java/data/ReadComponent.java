package data;

import dependencyCrawler.DependencyCrawlerInput;

public interface ReadComponent extends Component {
    Component getActualComponent();
    DependencyCrawlerInput.Type getType();
}
