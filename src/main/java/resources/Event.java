package resources;

import java.time.ZonedDateTime;

public class Event {
    private ZonedDateTime zonedDateTime;
    private String description;
    private int id;

    public Event(){
        this.zonedDateTime = ZonedDateTime.now();
        this.description = "no description";
    }

    public Event(String description, ZonedDateTime zonedDateTime) {
        this.zonedDateTime = zonedDateTime;
        this.description = description;
    }

    public ZonedDateTime getZonedDateTime() {
        return zonedDateTime;
    }

    public String getDescription() {
        return description;
    }

    public void setZonedDateTime(ZonedDateTime zonedDateTime) {
        this.zonedDateTime = zonedDateTime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(int id){
        this.id = id;
    }

    public int getId(){
        return id;
    }

    @Override
    public String toString() {
        return zonedDateTime.getHour() + ":00 " + description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event)) return false;

        Event event = (Event) o;

        return getId() == event.getId() && !(getZonedDateTime() != null ? !getZonedDateTime().equals(event.getZonedDateTime()) : event.getZonedDateTime() != null) &&
                !(getDescription() != null ? !getDescription().equals(event.getDescription()) : event.getDescription() != null);

    }

    @Override
    public int hashCode() {
        int result = getZonedDateTime() != null ? getZonedDateTime().hashCode() : 0;
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + getId();
        return result;
    }
}