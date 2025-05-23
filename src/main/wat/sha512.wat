(module
  (memory (export "memory") 1)

  ;; registers
  (global $a (mut i64) (i64.const 0))
  (global $b (mut i64) (i64.const 0))
  (global $c (mut i64) (i64.const 0))
  (global $d (mut i64) (i64.const 0))
  (global $e (mut i64) (i64.const 0))
  (global $f (mut i64) (i64.const 0))
  (global $g (mut i64) (i64.const 0))
  (global $h (mut i64) (i64.const 0))

  (func $i64.bswap
    (param $b i64)
    (result i64)

    ;; 1 set, 4 get, 8 const, 10 bitwise
    (i64.or
      (local.get $b)
      (i64.const 0xFFFF0000FFFF0000)
      (i64.and)
      (i64.rotl (i64.const 16))

      (local.get $b)
      (i64.const 0x0000FFFF0000FFFF)
      (i64.and)
      (i64.rotr (i64.const 16)))

    (local.set $b)

    (i64.or
      (local.get $b)
      (i64.const 0x00FF00FF00FF00FF)
      (i64.and)
      (i64.rotl (i64.const 8))

      (local.get $b)
      (i64.const 0xFF00FF00FF00FF00)
      (i64.and)
      (i64.rotr (i64.const 8))))

  (func $four_round
    (param $w0 i64) (param $w1 i64) (param $w2 i64) (param $w3 i64)
    (param $k0 i64) (param $k1 i64) (param $k2 i64) (param $k3 i64)

    ;; precomputed values
    (local $T1 i64)
    (local $T2 i64)

    (local $ch_res i64)
    (local $maj_res i64)
    (local $big_sig0_res i64)
    (local $big_sig1_res i64)

    ;; precompute intermediate values
    ;; T1 = h + big_sig1(e) + ch(e, f, g) + K0 + W0
    ;; T2 = big_sig0(a) + Maj(a, b, c)

    ;; can compute 4 rounds independently

    (local.set $ch_res (i64.xor (i64.and (global.get $e) (global.get $f)) (i64.and (i64.xor (global.get $e) (i64.const -1)) (global.get $g))))
    (local.set $maj_res (i64.xor (i64.xor (i64.and (global.get $a) (global.get $b)) (i64.and (global.get $a) (global.get $c))) (i64.and (global.get $b) (global.get $c))))
    (local.set $big_sig0_res (i64.xor (i64.xor (i64.rotr (global.get $a) (i64.const 28)) (i64.rotr (global.get $a) (i64.const 34))) (i64.rotr (global.get $a) (i64.const 39))))
    (local.set $big_sig1_res (i64.xor (i64.xor (i64.rotr (global.get $e) (i64.const 14)) (i64.rotr (global.get $e) (i64.const 18))) (i64.rotr (global.get $e) (i64.const 41))))
    (local.set $T1 (i64.add (i64.add (i64.add (i64.add (global.get $h) (local.get $ch_res)) (local.get $big_sig1_res)) (local.get $w0)) (local.get $k0)))
    (local.set $T2 (i64.add (local.get $big_sig0_res) (local.get $maj_res)))

    (global.set $h (i64.add (global.get $d) (local.get $T1)))
    (global.set $d (i64.add (local.get $T1) (local.get $T2)))

    (local.set $ch_res (i64.xor (i64.and (global.get $h) (global.get $e)) (i64.and (i64.xor (global.get $h) (i64.const -1)) (global.get $f))))
    (local.set $maj_res (i64.xor (i64.xor (i64.and (global.get $d) (global.get $a)) (i64.and (global.get $d) (global.get $b))) (i64.and (global.get $a) (global.get $b))))
    (local.set $big_sig0_res (i64.xor (i64.xor (i64.rotr (global.get $d) (i64.const 28)) (i64.rotr (global.get $d) (i64.const 34))) (i64.rotr (global.get $d) (i64.const 39))))
    (local.set $big_sig1_res (i64.xor (i64.xor (i64.rotr (global.get $h) (i64.const 14)) (i64.rotr (global.get $h) (i64.const 18))) (i64.rotr (global.get $h) (i64.const 41))))
    (local.set $T1 (i64.add (i64.add (i64.add (i64.add (global.get $g) (local.get $ch_res)) (local.get $big_sig1_res)) (local.get $w1)) (local.get $k1)))
    (local.set $T2 (i64.add (local.get $big_sig0_res) (local.get $maj_res)))

    (global.set $g (i64.add (global.get $c) (local.get $T1)))
    (global.set $c (i64.add (local.get $T1) (local.get $T2)))

    (local.set $ch_res (i64.xor (i64.and (global.get $g) (global.get $h)) (i64.and (i64.xor (global.get $g) (i64.const -1)) (global.get $e))))
    (local.set $maj_res (i64.xor (i64.xor (i64.and (global.get $c) (global.get $d)) (i64.and (global.get $c) (global.get $a))) (i64.and (global.get $d) (global.get $a))))
    (local.set $big_sig0_res (i64.xor (i64.xor (i64.rotr (global.get $c) (i64.const 28)) (i64.rotr (global.get $c) (i64.const 34))) (i64.rotr (global.get $c) (i64.const 39))))
    (local.set $big_sig1_res (i64.xor (i64.xor (i64.rotr (global.get $g) (i64.const 14)) (i64.rotr (global.get $g) (i64.const 18))) (i64.rotr (global.get $g) (i64.const 41))))
    (local.set $T1 (i64.add (i64.add (i64.add (i64.add (global.get $f) (local.get $ch_res)) (local.get $big_sig1_res)) (local.get $w2)) (local.get $k2)))
    (local.set $T2 (i64.add (local.get $big_sig0_res) (local.get $maj_res)))

    (global.set $f (i64.add (global.get $b) (local.get $T1)))
    (global.set $b (i64.add (local.get $T1) (local.get $T2)))

    (local.set $ch_res (i64.xor (i64.and (global.get $f) (global.get $g)) (i64.and (i64.xor (global.get $f) (i64.const -1)) (global.get $h))))
    (local.set $maj_res (i64.xor (i64.xor (i64.and (global.get $b) (global.get $c)) (i64.and (global.get $b) (global.get $d))) (i64.and (global.get $d) (global.get $c))))
    (local.set $big_sig0_res (i64.xor (i64.xor (i64.rotr (global.get $b) (i64.const 28)) (i64.rotr (global.get $b) (i64.const 34))) (i64.rotr (global.get $b) (i64.const 39))))
    (local.set $big_sig1_res (i64.xor (i64.xor (i64.rotr (global.get $f) (i64.const 14)) (i64.rotr (global.get $f) (i64.const 18))) (i64.rotr (global.get $f) (i64.const 41))))
    (local.set $T1 (i64.add (i64.add (i64.add (i64.add (global.get $e) (local.get $ch_res)) (local.get $big_sig1_res)) (local.get $w3)) (local.get $k3)))
    (local.set $T2 (i64.add (local.get $big_sig0_res) (local.get $maj_res)))

    (global.set $e (i64.add (global.get $a) (local.get $T1)))
    (global.set $a (i64.add (local.get $T1) (local.get $T2))))

  (func $expand
    (param $a i64) (param $b i64) (param $c i64) (param $d i64)
    (result i64)

    (i64.add
      (i64.add
        (i64.add
          (i64.xor
            (i64.xor
              (i64.rotr (local.get $a) (i64.const 19))
              (i64.rotr (local.get $a) (i64.const 61)))
            (i64.shr_u (local.get $a) (i64.const 6)))
          (local.get $b))
          (i64.xor
            (i64.xor
              (i64.rotr (local.get $c) (i64.const 1))
              (i64.rotr (local.get $c) (i64.const 8)))
            (i64.shr_u (local.get $c) (i64.const 7)))
        (local.get $d))))

  (func $compress
    (param $ctx i32)

    (param $w0 i64) (param $w1 i64) (param $w2 i64) (param $w3 i64)
    (param $w4 i64) (param $w5 i64) (param $w6 i64) (param $w7 i64)
    (param $w8 i64) (param $w9 i64) (param $w10 i64) (param $w11 i64)
    (param $w12 i64) (param $w13 i64) (param $w14 i64) (param $w15 i64)

    ;;  store inital state
    (if (i64.eq (i64.load offset=208 (local.get $ctx)) (i64.const 0))
        (then
            (i64.store offset=0  (local.get $ctx) (i64.const 0x6a09e667f3bcc908))
            (i64.store offset=8  (local.get $ctx) (i64.const 0xbb67ae8584caa73b))
            (i64.store offset=16 (local.get $ctx) (i64.const 0x3c6ef372fe94f82b))
            (i64.store offset=24 (local.get $ctx) (i64.const 0xa54ff53a5f1d36f1))
            (i64.store offset=32 (local.get $ctx) (i64.const 0x510e527fade682d1))
            (i64.store offset=40 (local.get $ctx) (i64.const 0x9b05688c2b3e6c1f))
            (i64.store offset=48 (local.get $ctx) (i64.const 0x1f83d9abfb41bd6b))
            (i64.store offset=56 (local.get $ctx) (i64.const 0x5be0cd19137e2179))
            (i64.store offset=208 (local.get $ctx) (i64.const 1))))

    ;; load previous hash state into registers
    (global.set $a (i64.load offset=0 (local.get $ctx)))
    (global.set $b (i64.load offset=8 (local.get $ctx)))
    (global.set $c (i64.load offset=16 (local.get $ctx)))
    (global.set $d (i64.load offset=24 (local.get $ctx)))
    (global.set $e (i64.load offset=32 (local.get $ctx)))
    (global.set $f (i64.load offset=40 (local.get $ctx)))
    (global.set $g (i64.load offset=48 (local.get $ctx)))
    (global.set $h (i64.load offset=56 (local.get $ctx)))

    (local.set $w0  (call $i64.bswap (local.get $w0 )))
    (local.set $w1  (call $i64.bswap (local.get $w1 )))
    (local.set $w2  (call $i64.bswap (local.get $w2 )))
    (local.set $w3  (call $i64.bswap (local.get $w3 )))
    (local.set $w4  (call $i64.bswap (local.get $w4 )))
    (local.set $w5  (call $i64.bswap (local.get $w5 )))
    (local.set $w6  (call $i64.bswap (local.get $w6 )))
    (local.set $w7  (call $i64.bswap (local.get $w7 )))
    (local.set $w8  (call $i64.bswap (local.get $w8 )))
    (local.set $w9  (call $i64.bswap (local.get $w9 )))
    (local.set $w10 (call $i64.bswap (local.get $w10)))
    (local.set $w11 (call $i64.bswap (local.get $w11)))
    (local.set $w12 (call $i64.bswap (local.get $w12)))
    (local.set $w13 (call $i64.bswap (local.get $w13)))
    (local.set $w14 (call $i64.bswap (local.get $w14)))
    (local.set $w15 (call $i64.bswap (local.get $w15)))

    (call $four_round (local.get $w0)  (local.get $w1)  (local.get $w2)  (local.get $w3)  (i64.const 0x428a2f98d728ae22) (i64.const 0x7137449123ef65cd) (i64.const 0xb5c0fbcfec4d3b2f) (i64.const 0xe9b5dba58189dbbc))
    (call $four_round (local.get $w4)  (local.get $w5)  (local.get $w6)  (local.get $w7)  (i64.const 0x3956c25bf348b538) (i64.const 0x59f111f1b605d019) (i64.const 0x923f82a4af194f9b) (i64.const 0xab1c5ed5da6d8118))
    (call $four_round (local.get $w8)  (local.get $w9)  (local.get $w10) (local.get $w11) (i64.const 0xd807aa98a3030242) (i64.const 0x12835b0145706fbe) (i64.const 0x243185be4ee4b28c) (i64.const 0x550c7dc3d5ffb4e2))
    (call $four_round (local.get $w12) (local.get $w13) (local.get $w14) (local.get $w15) (i64.const 0x72be5d74f27b896f) (i64.const 0x80deb1fe3b1696b1) (i64.const 0x9bdc06a725c71235) (i64.const 0xc19bf174cf692694))

    (local.set $w0  (call $expand (local.get $w14) (local.get $w9)  (local.get $w1)  (local.get $w0)))
    (local.set $w1  (call $expand (local.get $w15) (local.get $w10) (local.get $w2)  (local.get $w1)))
    (local.set $w2  (call $expand (local.get $w0 ) (local.get $w11) (local.get $w3)  (local.get $w2)))
    (local.set $w3  (call $expand (local.get $w1 ) (local.get $w12) (local.get $w4)  (local.get $w3)))
    (local.set $w4  (call $expand (local.get $w2 ) (local.get $w13) (local.get $w5)  (local.get $w4)))
    (local.set $w5  (call $expand (local.get $w3 ) (local.get $w14) (local.get $w6)  (local.get $w5)))
    (local.set $w6  (call $expand (local.get $w4 ) (local.get $w15) (local.get $w7)  (local.get $w6)))
    (local.set $w7  (call $expand (local.get $w5 ) (local.get $w0)  (local.get $w8)  (local.get $w7)))
    (local.set $w8  (call $expand (local.get $w6 ) (local.get $w1)  (local.get $w9)  (local.get $w8)))
    (local.set $w9  (call $expand (local.get $w7 ) (local.get $w2)  (local.get $w10) (local.get $w9)))
    (local.set $w10 (call $expand (local.get $w8 ) (local.get $w3)  (local.get $w11) (local.get $w10)))
    (local.set $w11 (call $expand (local.get $w9 ) (local.get $w4)  (local.get $w12) (local.get $w11)))
    (local.set $w12 (call $expand (local.get $w10) (local.get $w5)  (local.get $w13) (local.get $w12)))
    (local.set $w13 (call $expand (local.get $w11) (local.get $w6)  (local.get $w14) (local.get $w13)))
    (local.set $w14 (call $expand (local.get $w12) (local.get $w7)  (local.get $w15) (local.get $w14)))
    (local.set $w15 (call $expand (local.get $w13) (local.get $w8)  (local.get $w0)  (local.get $w15)))

    (call $four_round (local.get $w0)  (local.get $w1)  (local.get $w2)  (local.get $w3)  (i64.const 0xe49b69c19ef14ad2) (i64.const 0xefbe4786384f25e3) (i64.const 0x0fc19dc68b8cd5b5) (i64.const 0x240ca1cc77ac9c65))
    (call $four_round (local.get $w4)  (local.get $w5)  (local.get $w6)  (local.get $w7)  (i64.const 0x2de92c6f592b0275) (i64.const 0x4a7484aa6ea6e483) (i64.const 0x5cb0a9dcbd41fbd4) (i64.const 0x76f988da831153b5))
    (call $four_round (local.get $w8)  (local.get $w9)  (local.get $w10) (local.get $w11) (i64.const 0x983e5152ee66dfab) (i64.const 0xa831c66d2db43210) (i64.const 0xb00327c898fb213f) (i64.const 0xbf597fc7beef0ee4))
    (call $four_round (local.get $w12) (local.get $w13) (local.get $w14) (local.get $w15) (i64.const 0xc6e00bf33da88fc2) (i64.const 0xd5a79147930aa725) (i64.const 0x06ca6351e003826f) (i64.const 0x142929670a0e6e70))

    (local.set $w0  (call $expand (local.get $w14) (local.get $w9)  (local.get $w1)  (local.get $w0)))
    (local.set $w1  (call $expand (local.get $w15) (local.get $w10) (local.get $w2)  (local.get $w1)))
    (local.set $w2  (call $expand (local.get $w0)  (local.get $w11) (local.get $w3)  (local.get $w2)))
    (local.set $w3  (call $expand (local.get $w1)  (local.get $w12) (local.get $w4)  (local.get $w3)))
    (local.set $w4  (call $expand (local.get $w2)  (local.get $w13) (local.get $w5)  (local.get $w4)))
    (local.set $w5  (call $expand (local.get $w3)  (local.get $w14) (local.get $w6)  (local.get $w5)))
    (local.set $w6  (call $expand (local.get $w4)  (local.get $w15) (local.get $w7)  (local.get $w6)))
    (local.set $w7  (call $expand (local.get $w5)  (local.get $w0)  (local.get $w8)  (local.get $w7)))
    (local.set $w8  (call $expand (local.get $w6)  (local.get $w1)  (local.get $w9)  (local.get $w8)))
    (local.set $w9  (call $expand (local.get $w7)  (local.get $w2)  (local.get $w10) (local.get $w9)))
    (local.set $w10 (call $expand (local.get $w8)  (local.get $w3)  (local.get $w11) (local.get $w10)))
    (local.set $w11 (call $expand (local.get $w9)  (local.get $w4)  (local.get $w12) (local.get $w11)))
    (local.set $w12 (call $expand (local.get $w10) (local.get $w5)  (local.get $w13) (local.get $w12)))
    (local.set $w13 (call $expand (local.get $w11) (local.get $w6)  (local.get $w14) (local.get $w13)))
    (local.set $w14 (call $expand (local.get $w12) (local.get $w7)  (local.get $w15) (local.get $w14)))
    (local.set $w15 (call $expand (local.get $w13) (local.get $w8)  (local.get $w0)  (local.get $w15)))


    (call $four_round (local.get $w0)  (local.get $w1)  (local.get $w2)  (local.get $w3)  (i64.const 0x27b70a8546d22ffc) (i64.const 0x2e1b21385c26c926) (i64.const 0x4d2c6dfc5ac42aed) (i64.const 0x53380d139d95b3df))
    (call $four_round (local.get $w4)  (local.get $w5)  (local.get $w6)  (local.get $w7)  (i64.const 0x650a73548baf63de) (i64.const 0x766a0abb3c77b2a8) (i64.const 0x81c2c92e47edaee6) (i64.const 0x92722c851482353b))
    (call $four_round (local.get $w8)  (local.get $w9)  (local.get $w10) (local.get $w11) (i64.const 0xa2bfe8a14cf10364) (i64.const 0xa81a664bbc423001) (i64.const 0xc24b8b70d0f89791) (i64.const 0xc76c51a30654be30))
    (call $four_round (local.get $w12) (local.get $w13) (local.get $w14) (local.get $w15) (i64.const 0xd192e819d6ef5218) (i64.const 0xd69906245565a910) (i64.const 0xf40e35855771202a) (i64.const 0x106aa07032bbd1b8))

    (local.set $w0  (call $expand (local.get $w14) (local.get $w9)  (local.get $w1)  (local.get $w0)))
    (local.set $w1  (call $expand (local.get $w15) (local.get $w10) (local.get $w2)  (local.get $w1)))
    (local.set $w2  (call $expand (local.get $w0)  (local.get $w11) (local.get $w3)  (local.get $w2)))
    (local.set $w3  (call $expand (local.get $w1)  (local.get $w12) (local.get $w4)  (local.get $w3)))
    (local.set $w4  (call $expand (local.get $w2)  (local.get $w13) (local.get $w5)  (local.get $w4)))
    (local.set $w5  (call $expand (local.get $w3)  (local.get $w14) (local.get $w6)  (local.get $w5)))
    (local.set $w6  (call $expand (local.get $w4)  (local.get $w15) (local.get $w7)  (local.get $w6)))
    (local.set $w7  (call $expand (local.get $w5)  (local.get $w0)  (local.get $w8)  (local.get $w7)))
    (local.set $w8  (call $expand (local.get $w6)  (local.get $w1)  (local.get $w9)  (local.get $w8)))
    (local.set $w9  (call $expand (local.get $w7)  (local.get $w2)  (local.get $w10) (local.get $w9)))
    (local.set $w10 (call $expand (local.get $w8)  (local.get $w3)  (local.get $w11) (local.get $w10)))
    (local.set $w11 (call $expand (local.get $w9)  (local.get $w4)  (local.get $w12) (local.get $w11)))
    (local.set $w12 (call $expand (local.get $w10) (local.get $w5)  (local.get $w13) (local.get $w12)))
    (local.set $w13 (call $expand (local.get $w11) (local.get $w6)  (local.get $w14) (local.get $w13)))
    (local.set $w14 (call $expand (local.get $w12) (local.get $w7)  (local.get $w15) (local.get $w14)))
    (local.set $w15 (call $expand (local.get $w13) (local.get $w8)  (local.get $w0)  (local.get $w15)))

    (call $four_round (local.get $w0)  (local.get $w1)  (local.get $w2)  (local.get $w3)  (i64.const 0x19a4c116b8d2d0c8) (i64.const 0x1e376c085141ab53) (i64.const 0x2748774cdf8eeb99) (i64.const 0x34b0bcb5e19b48a8))
    (call $four_round (local.get $w4)  (local.get $w5)  (local.get $w6)  (local.get $w7)  (i64.const 0x391c0cb3c5c95a63) (i64.const 0x4ed8aa4ae3418acb) (i64.const 0x5b9cca4f7763e373) (i64.const 0x682e6ff3d6b2b8a3))
    (call $four_round (local.get $w8)  (local.get $w9)  (local.get $w10) (local.get $w11) (i64.const 0x748f82ee5defb2fc) (i64.const 0x78a5636f43172f60) (i64.const 0x84c87814a1f0ab72) (i64.const 0x8cc702081a6439ec))
    (call $four_round (local.get $w12) (local.get $w13) (local.get $w14) (local.get $w15) (i64.const 0x90befffa23631e28) (i64.const 0xa4506cebde82bde9) (i64.const 0xbef9a3f7b2c67915) (i64.const 0xc67178f2e372532b))

    (local.set $w0  (call $expand (local.get $w14) (local.get $w9)  (local.get $w1)  (local.get $w0)))
    (local.set $w1  (call $expand (local.get $w15) (local.get $w10) (local.get $w2)  (local.get $w1)))
    (local.set $w2  (call $expand (local.get $w0)  (local.get $w11) (local.get $w3)  (local.get $w2)))
    (local.set $w3  (call $expand (local.get $w1)  (local.get $w12) (local.get $w4)  (local.get $w3)))
    (local.set $w4  (call $expand (local.get $w2)  (local.get $w13) (local.get $w5)  (local.get $w4)))
    (local.set $w5  (call $expand (local.get $w3)  (local.get $w14) (local.get $w6)  (local.get $w5)))
    (local.set $w6  (call $expand (local.get $w4)  (local.get $w15) (local.get $w7)  (local.get $w6)))
    (local.set $w7  (call $expand (local.get $w5)  (local.get $w0)  (local.get $w8)  (local.get $w7)))
    (local.set $w8  (call $expand (local.get $w6)  (local.get $w1)  (local.get $w9)  (local.get $w8)))
    (local.set $w9  (call $expand (local.get $w7)  (local.get $w2)  (local.get $w10) (local.get $w9)))
    (local.set $w10 (call $expand (local.get $w8)  (local.get $w3)  (local.get $w11) (local.get $w10)))
    (local.set $w11 (call $expand (local.get $w9)  (local.get $w4)  (local.get $w12) (local.get $w11)))
    (local.set $w12 (call $expand (local.get $w10) (local.get $w5)  (local.get $w13) (local.get $w12)))
    (local.set $w13 (call $expand (local.get $w11) (local.get $w6)  (local.get $w14) (local.get $w13)))
    (local.set $w14 (call $expand (local.get $w12) (local.get $w7)  (local.get $w15) (local.get $w14)))
    (local.set $w15 (call $expand (local.get $w13) (local.get $w8)  (local.get $w0)  (local.get $w15)))

    (call $four_round (local.get $w0)  (local.get $w1)  (local.get $w2)  (local.get $w3)  (i64.const 0xca273eceea26619c) (i64.const 0xd186b8c721c0c207) (i64.const 0xeada7dd6cde0eb1e) (i64.const 0xf57d4f7fee6ed178))
    (call $four_round (local.get $w4)  (local.get $w5)  (local.get $w6)  (local.get $w7)  (i64.const 0x06f067aa72176fba) (i64.const 0x0a637dc5a2c898a6) (i64.const 0x113f9804bef90dae) (i64.const 0x1b710b35131c471b))
    (call $four_round (local.get $w8)  (local.get $w9)  (local.get $w10) (local.get $w11) (i64.const 0x28db77f523047d84) (i64.const 0x32caab7b40c72493) (i64.const 0x3c9ebe0a15c9bebc) (i64.const 0x431d67c49c100d4c))
    (call $four_round (local.get $w12) (local.get $w13) (local.get $w14) (local.get $w15) (i64.const 0x4cc5d4becb3e42b6) (i64.const 0x597f299cfc657e2a) (i64.const 0x5fcb6fab3ad6faec) (i64.const 0x6c44198c4a475817))

    ;; store hash values
    (i64.store offset=0  (local.get $ctx) (i64.add (i64.load offset=0  (local.get $ctx)) (global.get $a)))
    (i64.store offset=8  (local.get $ctx) (i64.add (i64.load offset=8  (local.get $ctx)) (global.get $b)))
    (i64.store offset=16 (local.get $ctx) (i64.add (i64.load offset=16 (local.get $ctx)) (global.get $c)))
    (i64.store offset=24 (local.get $ctx) (i64.add (i64.load offset=24 (local.get $ctx)) (global.get $d)))
    (i64.store offset=32 (local.get $ctx) (i64.add (i64.load offset=32 (local.get $ctx)) (global.get $e)))
    (i64.store offset=40 (local.get $ctx) (i64.add (i64.load offset=40 (local.get $ctx)) (global.get $f)))
    (i64.store offset=48 (local.get $ctx) (i64.add (i64.load offset=48 (local.get $ctx)) (global.get $g)))
    (i64.store offset=56 (local.get $ctx) (i64.add (i64.load offset=56 (local.get $ctx)) (global.get $h))))

  (func $sha512 (export "sha512") (param $ctx i32) (param $roi i32) (param $length i32) (param $final i32)
    ;; (result i32)

    ;;    schema  208 bytes
    ;;     0..64  hash state
    ;;    64..80  number of bytes read across all updates (128bit)
    ;;   80..208  store words between updates
    ;;  208..216  init flag

    (local $bytes_read i64)
    (local $bytes_read_overflow i64)
    (local $check_overflow i64)
    (local $last_word i64)
    (local $tail i64)

    ;; expanded message schedule
    (local $w0 i64)  (local $w1 i64)  (local $w2 i64)  (local $w3 i64)
    (local $w4 i64)  (local $w5 i64)  (local $w6 i64)  (local $w7 i64)
    (local $w8 i64)  (local $w9 i64)  (local $w10 i64) (local $w11 i64)
    (local $w12 i64) (local $w13 i64) (local $w14 i64) (local $w15 i64)

    (local.set $bytes_read (i64.load offset=64 (local.get $ctx)))
    (local.set $bytes_read_overflow (i64.load offset=72 (local.get $ctx)))
    (local.set $tail (i64.add (i64.and (local.get $bytes_read) (i64.const 0x7f)) (i64.extend_i32_u (local.get $length))))

    ;; load current block position
    (local.set $check_overflow (local.get $bytes_read))
    (local.set $bytes_read (i64.add (local.get $bytes_read) (i64.extend_i32_u (local.get $length))))
    (i64.store offset=64 (local.get $ctx) (local.get $bytes_read))

    ;; carry n > 64 bits for i128 length
    (if (i64.lt_u (local.get $bytes_read) (local.get $check_overflow))
        (then
            (local.set $bytes_read_overflow (i64.add (local.get $bytes_read_overflow) (i64.const 1)))
            (i64.store offset=72 (local.get $ctx) (local.get $bytes_read_overflow))))

    (block $finish
      (local.set $w0  (i64.load offset=80  (local.get $ctx)))
      (local.set $w1  (i64.load offset=88  (local.get $ctx)))
      (local.set $w2  (i64.load offset=96  (local.get $ctx)))
      (local.set $w3  (i64.load offset=104 (local.get $ctx)))
      (local.set $w4  (i64.load offset=112 (local.get $ctx)))
      (local.set $w5  (i64.load offset=120 (local.get $ctx)))
      (local.set $w6  (i64.load offset=128 (local.get $ctx)))
      (local.set $w7  (i64.load offset=136 (local.get $ctx)))
      (local.set $w8  (i64.load offset=144 (local.get $ctx)))
      (local.set $w9  (i64.load offset=152 (local.get $ctx)))
      (local.set $w10 (i64.load offset=160 (local.get $ctx)))
      (local.set $w11 (i64.load offset=168 (local.get $ctx)))
      (local.set $w12 (i64.load offset=176 (local.get $ctx)))
      (local.set $w13 (i64.load offset=184 (local.get $ctx)))
      (local.set $w14 (i64.load offset=192 (local.get $ctx)))
      (local.set $w15 (i64.load offset=200 (local.get $ctx)))

      (local.tee $tail (i64.sub (local.get $tail) (i64.const 128)))
      (i64.const 0)
      (i64.lt_s)
      (br_if $finish)

      (local.get $ctx)
      (local.get $w0 )
      (local.get $w1 )
      (local.get $w2 )
      (local.get $w3 )
      (local.get $w4 )
      (local.get $w5 )
      (local.get $w6 )
      (local.get $w7 )
      (local.get $w8 )
      (local.get $w9 )
      (local.get $w10)
      (local.get $w11)
      (local.get $w12)
      (local.get $w13)
      (local.get $w14)
      (local.get $w15)
      (call $compress)

      (loop $rest_of_input
        (local.set $w0  (i64.load offset=0   (local.get $roi)))
        (local.set $w1  (i64.load offset=8   (local.get $roi)))
        (local.set $w2  (i64.load offset=16  (local.get $roi)))
        (local.set $w3  (i64.load offset=24  (local.get $roi)))
        (local.set $w4  (i64.load offset=32  (local.get $roi)))
        (local.set $w5  (i64.load offset=40  (local.get $roi)))
        (local.set $w6  (i64.load offset=48  (local.get $roi)))
        (local.set $w7  (i64.load offset=56  (local.get $roi)))
        (local.set $w8  (i64.load offset=64  (local.get $roi)))
        (local.set $w9  (i64.load offset=72  (local.get $roi)))
        (local.set $w10 (i64.load offset=80  (local.get $roi)))
        (local.set $w11 (i64.load offset=88  (local.get $roi)))
        (local.set $w12 (i64.load offset=96  (local.get $roi)))
        (local.set $w13 (i64.load offset=104 (local.get $roi)))
        (local.set $w14 (i64.load offset=112 (local.get $roi)))
        (local.set $w15 (i64.load offset=120 (local.get $roi)))

        (local.set $roi (i32.add (local.get $roi) (i32.const 128)))

        (local.tee $tail (i64.sub (local.get $tail) (i64.const 128)))
        (i64.const 0)
        (i64.lt_s)
        (if
          (then
            (i64.store offset=80  (local.get $ctx) (local.get $w0))
            (i64.store offset=88  (local.get $ctx) (local.get $w1))
            (i64.store offset=96  (local.get $ctx) (local.get $w2))
            (i64.store offset=104 (local.get $ctx) (local.get $w3))
            (i64.store offset=112 (local.get $ctx) (local.get $w4))
            (i64.store offset=120 (local.get $ctx) (local.get $w5))
            (i64.store offset=128 (local.get $ctx) (local.get $w6))
            (i64.store offset=136 (local.get $ctx) (local.get $w7))
            (i64.store offset=144 (local.get $ctx) (local.get $w8))
            (i64.store offset=152 (local.get $ctx) (local.get $w9))
            (i64.store offset=160 (local.get $ctx) (local.get $w10))
            (i64.store offset=168 (local.get $ctx) (local.get $w11))
            (i64.store offset=176 (local.get $ctx) (local.get $w12))
            (i64.store offset=184 (local.get $ctx) (local.get $w13))
            (i64.store offset=192 (local.get $ctx) (local.get $w14))
            (i64.store offset=200 (local.get $ctx) (local.get $w15))
            (br $finish)))

        (local.get $ctx)
        (local.get $w0 )
        (local.get $w1 )
        (local.get $w2 )
        (local.get $w3 )
        (local.get $w4 )
        (local.get $w5 )
        (local.get $w6 )
        (local.get $w7 )
        (local.get $w8 )
        (local.get $w9 )
        (local.get $w10)
        (local.get $w11)
        (local.get $w12)
        (local.get $w13)
        (local.get $w14)
        (local.get $w15)
        (call $compress)

        (br $rest_of_input)))

    (if (i32.eq (local.get $final) (i32.const 1))
      (then
        (local.set $tail (i64.and (local.get $bytes_read) (i64.const 0x7f)))
        (local.set $last_word (i64.shl (i64.const 0x80) (i64.shl (i64.and (local.get $tail) (i64.const 0x7)) (i64.const 3))))

        (block $pad_end
                (block $13
                    (block $12
                        (block $11
                            (block $10
                                (block $9
                                    (block $8
                                        (block $7
                                            (block $6
                                                (block $5
                                                    (block $4
                                                        (block $3
                                                            (block $2
                                                                (block $1
                                                                    (block $0
                                                                        (block $15
                                                                            (block $14
                                                                                (block $switch
                                                                                    (i32.wrap_i64 (local.get $tail))
                                                                                    (i32.const 3)
                                                                                    (i32.shr_u)
                                                                                    (br_table $0 $1 $2 $3 $4 $5 $6 $7 $8 $9 $10 $11 $12 $13 $14 $15)))

                                                                                (local.get $last_word)
                                                                                (local.get $w14)
                                                                                (i64.or)
                                                                                (local.set $w14)
                                                                                (local.set $last_word (i64.const 0)))

                                                                            (local.get $last_word)
                                                                            (local.get $w15)
                                                                            (i64.or)
                                                                            (local.set $w15)
                                                                            (local.set $last_word (i64.const 0))

                                                                            ;; compress
                                                                            (local.get $ctx)
                                                                            (local.get $w0)
                                                                            (local.get $w1)
                                                                            (local.get $w2)
                                                                            (local.get $w3)
                                                                            (local.get $w4)
                                                                            (local.get $w5)
                                                                            (local.get $w6)
                                                                            (local.get $w7)
                                                                            (local.get $w8)
                                                                            (local.get $w9)
                                                                            (local.get $w10)
                                                                            (local.get $w11)
                                                                            (local.get $w12)
                                                                            (local.get $w13)
                                                                            (local.get $w14)
                                                                            (local.get $w15)
                                                                            (call $compress)

                                                                            (i64.store offset=64 (local.get $ctx) (local.get $bytes_read))

                                                                            ;; zero out words
                                                                            (local.set $w0  (i64.const 0))
                                                                            (local.set $w1  (i64.const 0))
                                                                            (local.set $w2  (i64.const 0))
                                                                            (local.set $w3  (i64.const 0))
                                                                            (local.set $w4  (i64.const 0))
                                                                            (local.set $w5  (i64.const 0))
                                                                            (local.set $w6  (i64.const 0))
                                                                            (local.set $w7  (i64.const 0))
                                                                            (local.set $w8  (i64.const 0))
                                                                            (local.set $w9  (i64.const 0))
                                                                            (local.set $w10 (i64.const 0))
                                                                            (local.set $w11 (i64.const 0))
                                                                            (local.set $w12 (i64.const 0))
                                                                            (local.set $w13 (i64.const 0))
                                                                            (local.set $w14 (i64.const 0))
                                                                            (local.set $w15 (i64.const 0)))

                                                                        (local.get $last_word)
                                                                        (local.get $w0)
                                                                        (i64.or)
                                                                        (local.set $w0)
                                                                        (local.set $last_word (i64.const 0)))

                                                                    (local.get $last_word)
                                                                    (local.get $w1)
                                                                    (i64.or)
                                                                    (local.set $w1)
                                                                    (local.set $last_word (i64.const 0)))

                                                                (local.get $last_word)
                                                                (local.get $w2)
                                                                (i64.or)
                                                                (local.set $w2)
                                                                (local.set $last_word (i64.const 0)))

                                                            (local.get $last_word)
                                                            (local.get $w3)
                                                            (i64.or)
                                                            (local.set $w3)
                                                            (local.set $last_word (i64.const 0)))

                                                        (local.get $last_word)
                                                        (local.get $w4)
                                                        (i64.or)
                                                        (local.set $w4)
                                                        (local.set $last_word (i64.const 0)))

                                                    (local.get $last_word)
                                                    (local.get $w5)
                                                    (i64.or)
                                                    (local.set $w5)
                                                    (local.set $last_word (i64.const 0)))

                                                (local.get $last_word)
                                                (local.get $w6)
                                                (i64.or)
                                                (local.set $w6)
                                                (local.set $last_word (i64.const 0)))

                                            (local.get $last_word)
                                            (local.get $w7)
                                            (i64.or)
                                            (local.set $w7)
                                            (local.set $last_word (i64.const 0)))

                                        (local.get $last_word)
                                        (local.get $w8)
                                        (i64.or)
                                        (local.set $w8)
                                        (local.set $last_word (i64.const 0)))

                                    (local.get $last_word)
                                    (local.get $w9)
                                    (i64.or)
                                    (local.set $w9)
                                    (local.set $last_word (i64.const 0)))

                                (local.get $last_word)
                                (local.get $w10)
                                (i64.or)
                                (local.set $w10)
                                (local.set $last_word (i64.const 0)))

                            (local.get $last_word)
                            (local.get $w11)
                            (i64.or)
                            (local.set $w11)
                            (local.set $last_word (i64.const 0)))

                        (local.get $last_word)
                        (local.get $w12)
                        (i64.or)
                        (local.set $w12)
                        (local.set $last_word (i64.const 0)))

                    (local.get $last_word)
                    (local.get $w13)
                    (i64.or)
                    (local.set $w13)
                    (local.set $last_word (i64.const 0)))

            ;; load upper limb of 128bit length
            (local.get $bytes_read)
            (i64.const 61)
            (i64.shr_u)
            (local.get $bytes_read_overflow)
            (i64.const 3)
            (i64.shr_u)
            (i64.add)
            (call $i64.bswap)
            (local.set $w14)

            ;; load lower limb of 128bit length
            (local.set $w15 (call $i64.bswap (i64.mul (local.get $bytes_read) (i64.const 8))))

            (local.get $ctx)
            (local.get $w0)
            (local.get $w1)
            (local.get $w2)
            (local.get $w3)
            (local.get $w4)
            (local.get $w5)
            (local.get $w6)
            (local.get $w7)
            (local.get $w8)
            (local.get $w9)
            (local.get $w10)
            (local.get $w11)
            (local.get $w12)
            (local.get $w13)
            (local.get $w14)
            (local.get $w15)
            (call $compress)

            (local.get $ctx)
            (i64.load offset=0 (local.get $ctx))
            (call $i64.bswap)
            (i64.store offset=0)

            (local.get $ctx)
            (i64.load offset=8 (local.get $ctx))
            (call $i64.bswap)
            (i64.store offset=8)

            (local.get $ctx)
            (i64.load offset=16 (local.get $ctx))
            (call $i64.bswap)
            (i64.store offset=16)

            (local.get $ctx)
            (i64.load offset=24 (local.get $ctx))
            (call $i64.bswap)
            (i64.store offset=24)

            (local.get $ctx)
            (i64.load offset=32 (local.get $ctx))
            (call $i64.bswap)
            (i64.store offset=32)

            (local.get $ctx)
            (i64.load offset=40 (local.get $ctx))
            (call $i64.bswap)
            (i64.store offset=40)

            (local.get $ctx)
            (i64.load offset=48 (local.get $ctx))
            (call $i64.bswap)
            (i64.store offset=48)

            (local.get $ctx)
            (i64.load offset=56 (local.get $ctx))
            (call $i64.bswap)
            (i64.store offset=56)))))
