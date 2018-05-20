package com.lxdnz.nz.ariaorienteering.model

import com.google.android.gms.tasks.Task
import com.lxdnz.nz.ariaorienteering.model.types.ImageType
import com.lxdnz.nz.ariaorienteering.tasks.MarkerTask

class Marker {
    var id = 0
    var imageType = ImageType.DEFAULT
    var lat = 0.0
    var lon = 0.0

    constructor() // default constructor for Firebase

    constructor(id: Int, imageType: ImageType, lat: Double, lon: Double) {
        this.id = id
        this.imageType = imageType
        this.lat = lat
        this.lon = lon
    }

    companion object: MarkerFactory {
        override fun create(id: Int, imageType: ImageType, lat: Double, lon: Double): Marker {
            val marker = Marker (id, imageType, lat, lon)
            // Do Marker add to DB task
            return MarkerTask.createTask(marker).result
        }

        override fun retrieve(id: Int): Task<Marker> {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun update(marker: Marker) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }

        override fun delete(id: Int) {
            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        }


    }

}

interface MarkerFactory {
    fun create(id: Int, imageType: ImageType, lat: Double, lon: Double ): Marker
    fun retrieve(id: Int): Task<Marker>
    fun update(marker: Marker)
    fun delete(id: Int)
}