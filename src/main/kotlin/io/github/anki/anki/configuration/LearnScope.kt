package io.github.anki.anki.configuration

import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.config.Scope
import java.time.LocalTime
import java.time.LocalTime.now
import java.util.concurrent.ConcurrentHashMap


class LearnScope: Scope {

    private var scopedLearnedObjects: ConcurrentHashMap<String, Pair<LocalTime, Any>> = ConcurrentHashMap()
    private var destructionCallbacks: ConcurrentHashMap<String, Runnable> = ConcurrentHashMap()
    private var hoursFromLastUpdateDefault: Int = 24

    override fun get(name: String, objectFactory: ObjectFactory<*>): Any {

        if(scopedLearnedObjects.containsKey(name)) {
            var pair = scopedLearnedObjects[name]

            var deadLineTime = now().plusHours(hoursFromLastUpdateDefault.toLong())

            if (pair != null) {
                if (pair.first.isBefore(deadLineTime)) {
                    this.scopedLearnedObjects.put(name, Pair(now(), objectFactory.`object`))
                }
            }
        }
        else {
            this.scopedLearnedObjects.put(name, Pair(now(), objectFactory.`object`))
        }
        return scopedLearnedObjects.get(name)!!.second;
    }

    override fun remove(name: String): Any? {
        destructionCallbacks.remove(name);
        return scopedLearnedObjects.remove(name);
    }

    override fun registerDestructionCallback(name: String, callback: Runnable) {
        destructionCallbacks.put(name, callback);
    }

    override fun resolveContextualObject(key: String): Any? {
        return null
    }

    override fun getConversationId(): String {
        return "learn"
    }
}