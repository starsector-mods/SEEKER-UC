package data.campaign.intel.bar;

import com.fs.starfarer.api.impl.campaign.intel.bar.PortsideBarEvent;
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEventCreator;

public class SKR_derelictRumorBarEventCreator extends BaseBarEventCreator {

    @Override
    public PortsideBarEvent createBarEvent() {
        return new SKR_derelictRumorBarEvent();
    }

    @Override
    public float getBarEventFrequencyWeight() {
        return 10f;
    }

    @Override
    public float getBarEventActiveDuration() {
        return 60f;
    }

    @Override
    public float getBarEventTimeoutDuration() {
        return 30f;
    }

    @Override
    public float getBarEventAcceptedTimeoutDuration() {
        return 180f;
    }
}
