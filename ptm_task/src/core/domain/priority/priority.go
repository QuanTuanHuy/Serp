/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package priority

type Dimension struct{
    Key string
    Value int32
}

type Policy struct{
    UserID int64
    Weights map[string]float64
}

type DimensionSpec struct{
    Key string
    Min float64
    Max float64
    DefaultWeight float64
}

var Registry = map[string]DimensionSpec{
    "urgency":    {Key: "urgency", Min: 0, Max: 100, DefaultWeight: 0.6},
    "importance": {Key: "importance", Min: 0, Max: 100, DefaultWeight: 0.4},
}

func normalize(v,min,max float64) float64{
    if max <= min { return v }
    x := (v - min) / (max - min)
    if x < 0 { x = 0 }
    if x > 1 { x = 1 }
    return x
}

func DefaultWeights() map[string]float64{
    m := map[string]float64{}
    for k, spec := range Registry{
        m[k] = spec.DefaultWeight
    }
    return m
}

func MergeWeights(system, user map[string]float64) map[string]float64{
    out := map[string]float64{}
    for k,v := range system { out[k] = v }
    for k,v := range user { out[k] = v }
    return out
}

func Score(dimensions []Dimension, p *Policy) float64{
    sum := 0.0
    if p == nil { p = &Policy{Weights: DefaultWeights()} }
    for _, d := range dimensions{
        w := p.Weights[d.Key]
        sum += float64(d.Value) * w
    }
    return sum
}

func ScoreNormalized(dimensions []Dimension, p *Policy) float64{
    sum := 0.0
    if p == nil { p = &Policy{Weights: DefaultWeights()} }
    for _, d := range dimensions{
        spec, ok := Registry[d.Key]
        v := float64(d.Value)
        if ok { v = normalize(v, spec.Min, spec.Max) }
        sum += v * p.Weights[d.Key]
    }
    return sum
}
