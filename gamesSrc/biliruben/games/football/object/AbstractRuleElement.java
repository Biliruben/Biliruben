package biliruben.games.football.object;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.sun.xml.internal.bind.v2.schemagen.Util;

public abstract class AbstractRuleElement<R extends AbstractRuleElement> {

    private String _title;
    private List<R> _entries;
    private AbstractRuleElement _parent;
    

    public AbstractRuleElement(String title) {
        this._title = title == null ? "" : title;
    }
    
    public List<R> getEntries() {
        return _entries;
    }
    
    public void addEntry(R entry) {
        if (this._entries == null) {
            this._entries = new ArrayList<R>();
        }
        entry.setParent(this);
        this._entries.add(entry);
    }
    
    private void setParent(AbstractRuleElement parent) {
        this._parent = parent;
    }
    
    public AbstractRuleElement getParent() {
        return this._parent;
    }
    
    public void setEntries(List<R> entries) {
        this._entries = new ArrayList<R>();
        for (R entry : entries) {
            addEntry(entry);
        }
    }
    
    public String getTitle() {
        return this._title;
    }
    
    public String print() {
        StringBuilder buff = new StringBuilder();
        buff.append(this);
        if (_entries != null) {
            for (R entry : _entries) {
                buff.append(entry.print());
            }
        }
        return buff.toString();
    }
    
    public String printUp() {
        StringBuilder buff = new StringBuilder();
        printUpInner(buff);
        return buff.toString();
    }
    
    protected void printUpInner(StringBuilder buffer) {
        if (_parent != null) {
            _parent.printUpInner(buffer);
        }
        buffer.append(toString());
    }
    
    public abstract AbstractRuleElement copy();

    public void merge(AbstractRuleElement toMerge) {
        if (toMerge == null) {
            // ignore, return
            return;
        }
        if (toMerge.getClass() != this.getClass()) {
            // bad idea
            throw new IllegalArgumentException(toMerge.getClass().getSimpleName() + " cannot be merged into " + this.getClass());
        }
        // this is a version of me
        for (Object obj : toMerge.getEntries() != null ? toMerge.getEntries() : new ArrayList()) {
            boolean merged = false;
            R entry = (R)obj;
            for (R mySection : getEntries() != null ? getEntries() : new ArrayList<R>()) {
                if (mySection.equals(entry)) {
                    mySection.merge(entry);
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                // just add it
                addEntry(entry);
            }
        }
    }
    
    /**
     * Takes a given child of any type and returns a skeleton copy of each ancestor
     */
    public AbstractRuleElement copyUp () {
        Stack<AbstractRuleElement> stack = new Stack<AbstractRuleElement>();
        stack.push(this);
        AbstractRuleElement current = this;
        while (current.getParent() != null) {
            stack.push(current.getParent());
            current = current.getParent();
        }
        // our stack is now parent-down. Pop each one, copy the first into
        // a holder. Create copies at each level and add them to the last parent
        AbstractRuleElement parent = stack.pop().copy();
        current = parent;
        while (!stack.isEmpty()) {
            AbstractRuleElement next = stack.pop().copy();
            current.addEntry(next);
            current = next;
        }
        
        return parent;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        AbstractRuleElement yours = (AbstractRuleElement)obj;
        return Util.equal(yours.getTitle(), _title);
    }

    @Override
    public int hashCode() {
        return _title != null ? _title.hashCode() * 3 : 0;
    }
}
