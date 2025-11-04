/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package priority

import (
	"maps"
	"strings"

	"github.com/serp/ptm-task/src/core/domain/entity"
)

type Policy struct {
	UserID  int64
	Weights map[string]float64
}

type DimensionSpec struct {
	Key           string
	Min           float64
	Max           float64
	DefaultWeight float64
}

var Registry = map[string]DimensionSpec{
	"urgency":    {Key: "urgency", Min: 0, Max: 100, DefaultWeight: 0.6},
	"importance": {Key: "importance", Min: 0, Max: 100, DefaultWeight: 0.4},
	"effort":     {Key: "effort", Min: 0, Max: 100, DefaultWeight: 0.2},
}

func normalize(v, min, max float64) float64 {
	if max <= min {
		return v
	}
	x := (v - min) / (max - min)
	if x < 0 {
		x = 0
	}
	if x > 1 {
		x = 1
	}
	return x
}

func DefaultWeights() map[string]float64 {
	m := map[string]float64{}
	for k, spec := range Registry {
		m[k] = spec.DefaultWeight
	}
	return m
}

func MergeWeights(system, user map[string]float64) map[string]float64 {
	out := map[string]float64{}
	maps.Copy(out, system)
	maps.Copy(out, user)
	return out
}

func Score(dimensions []entity.PriorityDimension, p *Policy) float64 {
	sum := 0.0
	if p == nil {
		p = &Policy{Weights: DefaultWeights()}
	}
	for _, d := range dimensions {
		w := p.Weights[d.Key]
		sum += float64(d.Value) * w
	}
	return sum
}

func ScoreNormalized(dimensions []entity.PriorityDimension, p *Policy) float64 {
	sum := 0.0
	if p == nil {
		p = &Policy{Weights: DefaultWeights()}
	}
	for _, d := range dimensions {
		key := strings.ToLower(d.Key)
		spec, ok := Registry[key]
		v := float64(d.Value)
		if ok {
			v = normalize(v, spec.Min, spec.Max)
		}
		sum += v * p.Weights[key]
	}
	return sum
}
