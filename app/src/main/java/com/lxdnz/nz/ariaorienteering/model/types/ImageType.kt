package com.lxdnz.nz.ariaorienteering.model.types

enum class ImageType {
    DEFAULT {
        override fun toString(): String {
            return "Default Image"
        }
    },
    STAR {
        override fun toString(): String {
            return "A Star"
        }
    },
    MOON{
        override fun toString(): String {
            return "The Moon"
        }
    };



}
