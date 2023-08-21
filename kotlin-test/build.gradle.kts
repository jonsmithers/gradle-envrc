import dev.smithers.envrc

plugins {
     id("dev.smithers.envrc")
}

println("kotlin envrc val A=${envrc["A"]}")
println("kotlin envrc val HI=${envrc["HI"]}")
