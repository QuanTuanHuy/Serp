package utils

func StringPtr(s string) *string {
	return &s
}

func IntPtr(i int) *int {
	return &i
}

func Int64Ptr(i int64) *int64 {
	return &i
}

func Float32Ptr(f float32) *float32 {
	return &f
}

func Float64Ptr(f float64) *float64 {
	return &f
}

func BoolPtr(b bool) *bool {
	return &b
}

func StringValue(s *string) string {
	if s == nil {
		return ""
	}
	return *s
}

func StringValueWithDefault(s *string, defaultValue string) string {
	if s == nil {
		return defaultValue
	}
	return *s
}

func IntValue(i *int) int {
	if i == nil {
		return 0
	}
	return *i
}

func IntValueWithDefault(i *int, defaultValue int) int {
	if i == nil {
		return defaultValue
	}
	return *i
}

func Int64Value(i *int64) int64 {
	if i == nil {
		return 0
	}
	return *i
}

func Float32Value(f *float32) float32 {
	if f == nil {
		return 0
	}
	return *f
}

func Float32ValueWithDefault(f *float32, defaultValue float32) float32 {
	if f == nil {
		return defaultValue
	}
	return *f
}

func Float64Value(f *float64) float64 {
	if f == nil {
		return 0
	}
	return *f
}

func BoolValue(b *bool) bool {
	if b == nil {
		return false
	}
	return *b
}

func IsNil(ptr interface{}) bool {
	return ptr == nil
}

func StringSliceFromPointers(ptrs []*string) []string {
	result := make([]string, 0, len(ptrs))
	for _, ptr := range ptrs {
		if ptr != nil {
			result = append(result, *ptr)
		}
	}
	return result
}

func StringPointersFromSlice(strs []string) []*string {
	result := make([]*string, len(strs))
	for i, str := range strs {
		result[i] = StringPtr(str)
	}
	return result
}
