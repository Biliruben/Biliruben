package biliruben.api;

public interface Command {
    
    public void execute();
    
    public void setArguments(String[] arguments);

}
